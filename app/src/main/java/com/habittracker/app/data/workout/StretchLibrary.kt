package com.habittracker.app.data.workout

data class Stretch(val name: String, val bodyPart: String, val description: String)

val stretchBodyParts = listOf("Neck", "Shoulders", "Back", "Chest", "Arms", "Hips", "Hamstrings", "Quads", "Calves")

val stretchLibrary = listOf(
    Stretch("Neck Tilt", "Neck", "Tilt your head toward one shoulder, hold, then switch sides."),
    Stretch("Neck Rotation", "Neck", "Slowly turn your head to look over each shoulder in turn."),

    Stretch("Shoulder Roll", "Shoulders", "Roll both shoulders forward, then backward, in slow circles."),
    Stretch("Cross-body Shoulder Stretch", "Shoulders", "Pull one arm across your chest with the other arm, hold."),
    Stretch("Overhead Triceps Stretch", "Shoulders", "Raise one arm overhead, bend the elbow, and gently pull with the other hand."),

    Stretch("Cat-Cow", "Back", "On hands and knees, alternate arching and rounding your spine."),
    Stretch("Child's Pose", "Back", "Kneel and sit back onto your heels, reaching your arms forward on the floor."),
    Stretch("Seated Spinal Twist", "Back", "Sitting, rotate your torso to one side using your arm for leverage, hold, then switch."),

    Stretch("Doorway Chest Stretch", "Chest", "Place a forearm on a doorframe and lean forward gently."),
    Stretch("Standing Chest Opener", "Chest", "Clasp your hands behind your back and lift your arms slightly, opening the chest."),

    Stretch("Wrist Flexor Stretch", "Arms", "Extend one arm, palm up, and gently pull the fingers back with the other hand."),
    Stretch("Triceps Stretch", "Arms", "Reach one arm overhead, bend the elbow behind your head, and press gently."),

    Stretch("Figure-Four Stretch", "Hips", "Lying on your back, cross one ankle over the opposite knee and pull the leg toward you."),
    Stretch("Hip Flexor Lunge", "Hips", "Step into a low lunge and press your hips forward, keeping the back leg straight."),
    Stretch("Butterfly Stretch", "Hips", "Sit with the soles of your feet together and gently press your knees toward the floor."),

    Stretch("Standing Hamstring Stretch", "Hamstrings", "Place one heel on a raised surface and hinge forward at the hips."),
    Stretch("Seated Forward Fold", "Hamstrings", "Sit with legs extended and reach toward your toes."),

    Stretch("Standing Quad Stretch", "Quads", "Stand on one leg, pull the other heel toward your glutes, hold."),
    Stretch("Kneeling Quad Stretch", "Quads", "From a kneeling lunge, pull the back foot toward your glutes."),

    Stretch("Calf Wall Stretch", "Calves", "Facing a wall, step one foot back and press the heel into the floor."),
    Stretch("Downward Dog", "Calves", "From hands and feet, push hips up and press heels toward the floor.")
)
