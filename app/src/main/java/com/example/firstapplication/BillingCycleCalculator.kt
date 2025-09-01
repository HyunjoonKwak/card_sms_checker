package com.example.firstapplication

import com.example.firstapplication.db.CardBillingCycle
import java.util.Calendar
import java.util.Date

object BillingCycleCalculator {

    data class BillingPeriod(
        val startDate: Date,
        val endDate: Date,
        val description: String
    )

    fun getCurrentBillingPeriod(billingCycle: CardBillingCycle): BillingPeriod {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Determine if we're in the current billing period or the next one
        val (periodStartYear, periodStartMonth) = if (currentDay >= billingCycle.cutoffDay) {
            // We're past the cutoff, so this is for next month's billing
            if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                Pair(calendar.get(Calendar.YEAR) + 1, Calendar.JANUARY)
            } else {
                Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
            }
        } else {
            // We're before the cutoff, so this is for current month's billing
            Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }

        // Calculate start date (cutoff day of previous month)
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.YEAR, periodStartYear)
        startCalendar.set(Calendar.MONTH, periodStartMonth)
        startCalendar.add(Calendar.MONTH, -1) // Go to previous month
        startCalendar.set(Calendar.DAY_OF_MONTH, billingCycle.cutoffDay)
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)

        // Calculate end date (cutoff day of current month)
        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.YEAR, periodStartYear)
        endCalendar.set(Calendar.MONTH, periodStartMonth)
        endCalendar.set(Calendar.DAY_OF_MONTH, billingCycle.cutoffDay)
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)

        val startDate = startCalendar.time
        val endDate = endCalendar.time

        val description = "${billingCycle.bankName} ${billingCycle.cardName} " +
                "${java.text.SimpleDateFormat("MM월", java.util.Locale.KOREA).format(startDate)}" +
                "${billingCycle.cutoffDay}일 ~ " +
                "${java.text.SimpleDateFormat("MM월", java.util.Locale.KOREA).format(endDate)}" +
                "${billingCycle.cutoffDay}일 (결제일: ${billingCycle.billingDay}일)"

        return BillingPeriod(startDate, endDate, description)
    }

    fun getBillingPeriodForMonth(billingCycle: CardBillingCycle, year: Int, month: Int): BillingPeriod {
        // Calculate start date (cutoff day of previous month)
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.YEAR, year)
        startCalendar.set(Calendar.MONTH, month)
        startCalendar.add(Calendar.MONTH, -1) // Go to previous month
        startCalendar.set(Calendar.DAY_OF_MONTH, billingCycle.cutoffDay)
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)

        // Calculate end date (cutoff day of target month)
        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.YEAR, year)
        endCalendar.set(Calendar.MONTH, month)
        endCalendar.set(Calendar.DAY_OF_MONTH, billingCycle.cutoffDay)
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)

        val startDate = startCalendar.time
        val endDate = endCalendar.time

        val description = "${billingCycle.bankName} ${billingCycle.cardName} " +
                "${java.text.SimpleDateFormat("MM월", java.util.Locale.KOREA).format(startDate)}" +
                "${billingCycle.cutoffDay}일 ~ " +
                "${java.text.SimpleDateFormat("MM월", java.util.Locale.KOREA).format(endDate)}" +
                "${billingCycle.cutoffDay}일 (결제일: ${billingCycle.billingDay}일)"

        return BillingPeriod(startDate, endDate, description)
    }

    fun getNextBillingDate(billingCycle: CardBillingCycle): Date {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // If we haven't reached this month's billing date, use this month
        // Otherwise, use next month
        if (currentDay < billingCycle.billingDay) {
            calendar.set(Calendar.DAY_OF_MONTH, billingCycle.billingDay)
        } else {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, billingCycle.billingDay)
        }

        calendar.set(Calendar.HOUR_OF_DAY, 9) // 9 AM for billing
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }
}