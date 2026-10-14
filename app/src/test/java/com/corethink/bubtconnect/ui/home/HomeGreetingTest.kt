package com.corethink.bubtconnect.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeGreetingTest {

    @Test
    fun greetingFollowsTimeOfDay() {
        assertEquals("Good morning,", HomeViewModel.greetingFor(5))
        assertEquals("Good morning,", HomeViewModel.greetingFor(11))
        assertEquals("Good afternoon,", HomeViewModel.greetingFor(12))
        assertEquals("Good evening,", HomeViewModel.greetingFor(20))
        assertEquals("Hello,", HomeViewModel.greetingFor(23))
        assertEquals("Hello,", HomeViewModel.greetingFor(2))
    }
}
