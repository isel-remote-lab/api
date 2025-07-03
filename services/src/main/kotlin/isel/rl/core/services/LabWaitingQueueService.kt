package isel.rl.core.services

import EventEmitter
import isel.rl.core.domain.events.Event
import isel.rl.core.repository.TransactionManager
import isel.rl.core.services.interfaces.ILabWaitingQueueService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service

@Service
data class LabWaitingQueueService(
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
) : ILabWaitingQueueService {
    override suspend fun pushUserIntoQueue(
        labId: Int,
        userId: Int,
        listener: EventEmitter,
    ) {
        transactionManager.run {
            // TODO: Handle a unexpected error if occurs
            it.labWaitingQueueRepository.addUserToLabQueue(labId, userId)

            listener.emit(
                Event.WaitingQueue(
                    eventId = System.currentTimeMillis(),
                    labId = labId.toString(),
                    waitingQueuePos = it.labWaitingQueueRepository.getUserQueuePosition(labId = labId, userId),
                ),
            )
        }

        LOG.info("Listening to channel: ${getChannelName(labId, userId)}")
        var message: String
        do {
            message = listenToChannel(getChannelName(labId, userId)).first()
            LOG.info("Received message from channel: $message")
            if (message == KEEP_IN_QUEUE) {
                transactionManager.run {
                    listener.emit(
                        Event.WaitingQueue(
                            eventId = System.currentTimeMillis(),
                            labId = labId.toString(),
                            waitingQueuePos =
                                it.labWaitingQueueRepository.getUserQueuePosition(
                                    labId = labId,
                                    userId,
                                ),
                        ),
                    )
                }
            } else {
                listener.emit(
                    Event.Message(
                        System.currentTimeMillis(),
                        "Entering in laboratory...",
                    ),
                )
            }
        } while (message == KEEP_IN_QUEUE)
    }

    override fun popUserFromQueue(labId: Int) {
        val nextUserInQueue: Int =
            transactionManager.run {
                // TODO: Handle a unexpected error if occurs
                return@run it.labWaitingQueueRepository.popLabQueue(labId)
            }

        LOG.info("Received popUserFromQueue: $nextUserInQueue")
        redisTemplate.convertAndSend(getChannelName(labId, nextUserInQueue), "")
    }

    override fun updateQueuePositions(labId: Int) {
        val usersInQueue =
            transactionManager.run {
                return@run it.labWaitingQueueRepository.getUsersInQueue(labId)
            }

        usersInQueue.forEach { userId ->
            redisTemplate.convertAndSend(
                getChannelName(labId, userId),
                KEEP_IN_QUEUE,
            )
        }
    }

    private fun listenToChannel(channel: String): Flow<String> =
        channelFlow {
            val topic = ChannelTopic(channel)
            val messageListener =
                MessageListener { message, _ ->
                    val content = String(message.body)
                    trySend(content)
                }

            redisMessageListenerContainer.addMessageListener(messageListener, topic)

            awaitClose {
                redisMessageListenerContainer.removeMessageListener(messageListener, topic)
            }
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(LabWaitingQueueService::class.java)

        private fun getChannelName(
            labId: Int,
            userId: Int,
        ): String = "lab:$labId:queue:$userId"

        private const val KEEP_IN_QUEUE = "keepInQueue"
    }
}
