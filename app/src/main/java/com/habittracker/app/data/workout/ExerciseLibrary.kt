package com.habittracker.app.data.workout

data class GymExercise(val name: String, val muscleGroup: String)

val muscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body")

val exerciseLibrary = listOf(
    GymExercise("Bench Press", "Chest"),
    GymExercise("Incline Bench Press", "Chest"),
    GymExercise("Push-up", "Chest"),
    GymExercise("Chest Fly", "Chest"),
    GymExercise("Cable Crossover", "Chest"),

    GymExercise("Pull-up", "Back"),
    GymExercise("Lat Pulldown", "Back"),
    GymExercise("Bent-over Row", "Back"),
    GymExercise("Seated Cable Row", "Back"),
    GymExercise("Deadlift", "Back"),

    GymExercise("Squat", "Legs"),
    GymExercise("Leg Press", "Legs"),
    GymExercise("Lunges", "Legs"),
    GymExercise("Leg Curl", "Legs"),
    GymExercise("Leg Extension", "Legs"),
    GymExercise("Calf Raise", "Legs"),

    GymExercise("Overhead Press", "Shoulders"),
    GymExercise("Lateral Raise", "Shoulders"),
    GymExercise("Front Raise", "Shoulders"),
    GymExercise("Face Pull", "Shoulders"),
    GymExercise("Shrug", "Shoulders"),

    GymExercise("Bicep Curl", "Arms"),
    GymExercise("Hammer Curl", "Arms"),
    GymExercise("Tricep Pushdown", "Arms"),
    GymExercise("Tricep Dip", "Arms"),
    GymExercise("Skull Crusher", "Arms"),

    GymExercise("Plank", "Core"),
    GymExercise("Crunch", "Core"),
    GymExercise("Russian Twist", "Core"),
    GymExercise("Hanging Leg Raise", "Core"),

    GymExercise("Burpee", "Full Body"),
    GymExercise("Kettlebell Swing", "Full Body"),
    GymExercise("Clean and Press", "Full Body"),
    GymExercise("Thruster", "Full Body")
)
