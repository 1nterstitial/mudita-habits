# Keep Room generated classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep data classes used in JSON export/import
-keep class com.mudita.habits.data.** { *; }
