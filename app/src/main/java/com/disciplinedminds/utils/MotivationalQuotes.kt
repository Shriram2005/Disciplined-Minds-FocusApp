package com.disciplinedminds.utils

import kotlin.random.Random

/**
 * Repository for motivational quotes displayed on the lock screen
 */
object MotivationalQuotes {
    
    private val quotes = listOf(
        // Quotes about closing the app and staying focused
        "Close this app and get back to what matters!",
        "Your future self will thank you for closing this now.",
        "Every minute on this app is a minute away from your goals. Close it!",
        "Distractions are temporary, but your goals are permanent. Stay focused!",
        "This app can wait. Your dreams cannot. Close it now!",
        "Be stronger than your distractions. Close this and refocus!",
        "You're here for a reason. Don't let this app steal your time!",
        "Success requires sacrifice. Close this app and get back to work!",
        "Your goals are waiting. This app isn't going anywhere. Close it!",
        "Remember why you started. Close this and stay disciplined!",
        "Champions don't get distracted. Close this app and stay on track!",
        "Every second counts. Close this app and make them count!",
        
        // Study motivation quotes
        "Study now, celebrate later!",
        "The more you study, the more opportunities you create.",
        "Studying is an investment that always pays the best interest.",
        "Knowledge is power. Keep studying, keep growing!",
        "Study hard today for a brighter tomorrow.",
        "Your education is your superpower. Use it wisely!",
        "Study like there's no tomorrow. Learn like you'll live forever.",
        "The pain of discipline is less than the pain of regret. Study now!",
        "Every page you read takes you closer to success.",
        "Study with purpose. Your future depends on it!",
        "Learning is a gift. Even when distraction is the wrapping.",
        "The library of success is built one study session at a time.",
        "Study smart, study hard, achieve greatness!",
        "Your GPA isn't everything, but your effort is. Keep studying!",
        "Focus on the textbook, not the notifications.",
        
        // General motivational quotes
        "Success is the sum of small efforts repeated day in and day out.",
        "Focus on being productive instead of busy.",
        "The secret of getting ahead is getting started.",
        "Don't watch the clock; do what it does. Keep going.",
        "The harder you work for something, the greater you'll feel when you achieve it.",
        "Great things never come from comfort zones.",
        "Don't stop when you're tired. Stop when you're done.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Do something today that your future self will thank you for.",
        "It's going to be hard, but hard does not mean impossible.",
        "Don't wait for opportunity. Create it.",
        "Push yourself, because no one else is going to do it for you.",
        "Sometimes later becomes never. Do it now.",
        "The key to success is to focus on goals, not obstacles.",
        "Stay focused and never give up.",
        "You don't have to be great to start, but you have to start to be great.",
        "Small progress is still progress.",
        "Action is the foundational key to all success.",
        "The future depends on what you do today.",
        "Strive for progress, not perfection.",
        "Believe you can and you're halfway there.",
        "Your only limit is you.",
        "Make each day your masterpiece.",
        "Discipline is the bridge between goals and accomplishment.",
        "You are capable of amazing things.",
        "Be stronger than your excuses.",
        "If you want it, work for it. It's that simple.",
        "Success is not final, failure is not fatal: It is the courage to continue that counts.",
        "Work hard in silence. Let success make the noise.",
        "The pain you feel today will be the strength you feel tomorrow.",
        "Don't wish for it, work for it.",
        "Education is the passport to the future.",
        "The beautiful thing about learning is that no one can take it away from you."
    )
    
    /**
     * Returns a random motivational quote
     */
    fun getRandomQuote(): String {
        return quotes[Random.nextInt(quotes.size)]
    }
    
    /**
     * Returns all available quotes (useful for testing)
     */
    fun getAllQuotes(): List<String> = quotes
}
