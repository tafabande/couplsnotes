package com.example.noteshare.util

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PAIRS = "pairs"
    const val COLLECTION_NOTES = "notes"
    const val COLLECTION_MOODS = "moods"
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_QUESTIONS = "questions"
    const val COLLECTION_MEMORIES = "memories"
    const val COLLECTION_VERSIONS = "versions"

    // Invite Code
    const val INVITE_CODE_LENGTH = 6
    const val INVITE_CODE_EXPIRY_HOURS = 48L

    // Note Types
    const val NOTE_TYPE_TEXT = "text"
    const val NOTE_TYPE_LIST = "list"
    const val NOTE_TYPE_CHECKLIST = "checklist"

    // Display Styles
    const val DISPLAY_PLAIN = "plain"
    const val DISPLAY_BIG_PICTURE = "big_picture"
    const val DISPLAY_FRAMED = "framed"

    // Event Types
    const val EVENT_BIRTHDAY = "birthday"
    const val EVENT_ANNIVERSARY = "anniversary"
    const val EVENT_DATE = "date"
    const val EVENT_CUSTOM = "custom"

    // Pair Status
    const val PAIR_STATUS_PENDING = "pending"
    const val PAIR_STATUS_ACTIVE = "active"
    const val PAIR_STATUS_DISCONNECTING = "disconnecting"
    const val PAIR_STATUS_DISSOLVED = "dissolved"

    // Mood Levels
    const val MOOD_VERY_SAD = 1
    const val MOOD_SAD = 2
    const val MOOD_NEUTRAL = 3
    const val MOOD_HAPPY = 4
    const val MOOD_VERY_HAPPY = 5

    // Predefined Tags
    val DEFAULT_TAGS = listOf(
        "travel", "food", "school", "work",
        "family", "date", "gift", "health",
        "shopping", "thoughts", "recipes", "passwords"
    )

    // Daily Questions Pool
    val DAILY_QUESTIONS = listOf(
        "What made you smile today?",
        "What are you grateful for right now?",
        "What's one thing you'd love to do together this week?",
        "What's your favorite memory of us?",
        "If you could go anywhere right now, where would it be?",
        "What's something new you learned recently?",
        "What song reminds you of me?",
        "What's the best part of your day so far?",
        "What's one thing on your bucket list?",
        "If we could have any superpower together, what would it be?",
        "What's your comfort food?",
        "What movie should we watch next?",
        "What's something small that always makes your day better?",
        "If you could relive one day, which would it be?",
        "What's a skill you'd love to learn?",
        "What's the funniest thing that happened to you recently?",
        "What does your perfect weekend look like?",
        "What's a book/show you'd recommend to me?",
        "What are you looking forward to most this month?",
        "What's one thing you appreciate about me?",
        "What would your dream vacation look like?",
        "What's a random fact about you I might not know?",
        "If you could eat only one cuisine forever, which would it be?",
        "What's a goal you're working toward right now?",
        "What's the kindest thing someone did for you recently?",
        "What's your earliest happy memory?",
        "If we had a theme song, what would it be?",
        "What's something that always cheers you up?",
        "What's the most beautiful place you've ever been?",
        "What are three words that describe how you feel right now?",
        "What tradition would you love us to start?",
        "What's your love language today?",
        "What's one thing that made you proud recently?",
        "If you could master any instrument, which one?",
        "What's a childhood game you'd love to play again?",
        "What does home mean to you?",
        "What scent brings back a happy memory?",
        "What's your current guilty pleasure?",
        "If you could send a letter to your future self, what would you say?",
        "What adventure should we plan next?"
    )

    // Sync
    const val SYNC_WORK_NAME = "noteshare_sync"
    const val SYNC_INTERVAL_MINUTES = 15L

    // Preferences
    const val PREF_USER_ID = "user_id"
    const val PREF_PAIR_ID = "pair_id"
    const val PREF_PARTNER_NAME = "partner_name"
    const val PREF_IS_DARK_MODE = "is_dark_mode"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_LAST_SYNC = "last_sync"
    const val PREF_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
}
