package com.celery.rika.data

import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.events.GroupMessageEvent
import xyz.cssxsh.pixiv.AgeLimit
import xyz.cssxsh.pixiv.SanityLevel

object Limits : AutoSavePluginData("limits") {

    override val serializersModule: SerializersModule = SerializersModule {
        contextual(SanityLevel::class, SanityLevel.serializer())
        contextual(AgeLimit::class, AgeLimit.serializer())
    }
    val groupSanityLevel: MutableMap<Long, SanityLevel> by value()
    val userSanityLevel: MutableMap<Long, MutableMap<Long, SanityLevel>> by value()
    val groupR18: MutableMap<Long, AgeLimit> by value()
    val userR18: MutableMap<Long, MutableMap<Long, AgeLimit>> by value()
    val setuUseCount: MutableMap<Long, MutableMap<Long, Int>> by value()
    private val setuUseLimit: MutableMap<Long, MutableMap<Long, Int>> by value()
    private val defaultSetuLimit: Int by value(5)

    fun getCountNow(userId: Long, groupId: Long): Int {
        if (setuUseCount[groupId] == null) {
            setuUseCount[groupId] = mutableMapOf(userId to 0)
        }
        if (setuUseCount[groupId]!![userId] == null) {
            setuUseCount[groupId]!![userId] = 0
        }
        return setuUseCount[groupId]!![userId]!!
    }

    fun getCountLimit(userId: Long, groupId: Long): Int {
        if (setuUseLimit[groupId] == null) {
            setuUseLimit[groupId] = mutableMapOf()
        }
        if (setuUseLimit[groupId]!![userId] == null) {
            setuUseLimit[groupId]!![userId] = defaultSetuLimit
        }
        return setuUseLimit[groupId]!![userId]!!
    }

    fun setCountLimit(userId: Long, groupId: Long, limit: Int) {
        if (setuUseLimit[groupId] == null) {
            setuUseLimit[groupId] = mutableMapOf()
        }
        setuUseLimit[groupId]!![userId] = limit
    }

    fun setAge(user: Long, subject: Long, age: AgeLimit) {
        when (user) {
            0L -> groupR18[subject] = age
            else -> userR18[subject]?.set(user, age) ?: userR18.set(subject, mutableMapOf(user to age))
        }
    }

    fun getAge(user: Long, subject: Long): AgeLimit {
        return userR18[subject]?.get(user) ?: groupR18[subject] ?: AgeLimit.ALL
    }

    fun getSan(user: Long, subject: Long): SanityLevel {
        return userSanityLevel[subject]?.get(user) ?: groupSanityLevel[subject] ?: SanityLevel.WHITE
    }

    fun setSan(user: Long, subject: Long, sanityLevel: SanityLevel, messageEvent: GroupMessageEvent? = null) {
        if (messageEvent != null)
            when (user) {
                0L -> groupSanityLevel[subject] = sanityLevel
                else -> {
                    if (groupSanityLevel[subject] == null) {
                        groupSanityLevel[subject] = SanityLevel.WHITE
                    }
                    userSanityLevel[subject]?.set(user, sanityLevel) ?: userSanityLevel.set(
                        subject,
                        mutableMapOf(user to sanityLevel)
                    )
                }
            }
        else
            when (user) {
                0L -> groupSanityLevel[subject] = sanityLevel
                else -> {
                    if (groupSanityLevel[subject] == null) {
                        groupSanityLevel[subject] = SanityLevel.WHITE
                    }
                    userSanityLevel[subject]?.set(user, sanityLevel) ?: userSanityLevel.set(
                        subject,
                        mutableMapOf(user to sanityLevel)
                    )
                }
            }

    }

    fun setCountNow(userId: Long, groupId: Long, count: Int) {
        setuUseCount[groupId]!![userId] = count
    }

    fun increaseCountNow(userId: Long, groupId: Long, delta: Int) {
        setuUseCount[groupId]!![userId] = delta + setuUseCount[groupId]!![userId]!!
        if (setuUseCount[groupId]!![userId]!! > getCountLimit(userId, groupId) + 1)
            setCountNow(userId, groupId, getCountLimit(userId, groupId))
    }

}