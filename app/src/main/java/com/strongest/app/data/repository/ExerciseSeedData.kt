package com.strongest.app.data.repository

import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.classifyExercise

/**
 * Bump this whenever you change the built-in exercise data (names, descriptions, instructions,
 * muscle group, equipment, or add new exercises) and want existing installs to pick up the change.
 * On the next launch, non-custom exercises are re-synced from the seed data without touching the
 * user's workout history, notes, routines, or any exercises they created themselves.
 */
const val EXERCISE_SEED_VERSION = 2

@Suppress("MagicNumber", "MaxLineLength")
private val rawExerciseSeedData: List<Exercise> = listOf(
    Exercise(
        1,
        "Barbell Flat Bench Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Classic flat bench press for overall chest development.",
        "Lie flat on the bench with your feet planted firmly. Grip the bar slightly wider than shoulder width. Lower the bar under control to your mid-chest. Press it back up to full lockout.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        2,
        "Barbell Incline Bench Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Incline bench press targeting the upper chest fibers.",
        "Set the bench to a 30 to 45 degree incline. Grip the bar slightly wider than shoulder width. Lower it under control to your upper chest. Press the bar back up until your arms are fully extended.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        3,
        "Barbell Decline Bench Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Decline bench press emphasizing the lower chest.",
        "Secure your feet at the top of the decline bench. Unrack the bar and hold it over your chest. Lower it under control to your lower chest. Press the bar back up to full extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        4,
        "Barbell Close-Grip Bench Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Narrow grip bench press targeting the inner chest and triceps.",
        "Lie flat and grip the bar about shoulder width apart. Lower the bar to your lower chest while keeping your elbows tucked. Press the bar back up by extending your arms. Maintain a controlled tempo throughout.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        5,
        "Dumbbell Flat Bench Press",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Flat dumbbell press allowing a greater range of motion.",
        "Lie flat on the bench holding a dumbbell in each hand at chest level. Plant your feet and brace your core. Press the dumbbells up until your arms are extended. Lower them back to chest level under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        6,
        "Dumbbell Incline Bench Press",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Incline dumbbell press for upper chest development.",
        "Set the bench to a 30 to 45 degree incline. Hold a dumbbell in each hand at upper chest level. Press the dumbbells upward until your arms are extended. Lower them back down under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        7,
        "Dumbbell Decline Bench Press",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Decline dumbbell press targeting the lower chest.",
        "Secure yourself on the decline bench with a dumbbell in each hand. Hold the dumbbells over your lower chest. Press them upward until your arms are extended. Lower them back down with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        8,
        "Dumbbell Flat Fly",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Flat bench fly for chest isolation and stretch.",
        "Lie flat on the bench holding a dumbbell in each hand above your chest. Keep a slight bend in your elbows. Open your arms wide until you feel a stretch across your chest. Bring the dumbbells back together over your chest."
    ),
    Exercise(
        9,
        "Dumbbell Incline Fly",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Incline fly targeting the upper chest fibers.",
        "Set the bench to an incline and hold a dumbbell in each hand above your chest. Keep a slight bend in your elbows. Lower your arms out wide until you feel a stretch. Squeeze the dumbbells back together over your chest."
    ),
    Exercise(
        10,
        "Dumbbell Decline Fly",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Decline fly emphasizing the lower chest.",
        "Secure yourself on the decline bench holding a dumbbell in each hand. Keep a slight bend in your elbows. Open your arms wide to stretch the lower chest. Bring the dumbbells back together over your chest."
    ),
    Exercise(
        11,
        "Dumbbell Pullover",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Dumbbell pullover for chest expansion and serratus activation.",
        "Lie perpendicular across the bench with your upper back supported. Hold one dumbbell with both hands above your chest. Lower it behind your head with a slight bend in your elbows. Pull it back over your chest under control.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.TRICEPS)
    ),
    Exercise(
        12,
        "Cable Crossover (High to Low)",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "High-to-low cable crossover targeting the lower chest.",
        "Set both cable pulleys to a high position and grab a handle in each hand. Step forward with a slight forward lean. Pull the handles down and together in an arc. Squeeze your chest at the bottom and return slowly."
    ),
    Exercise(
        13,
        "Cable Crossover (Low to High)",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Low-to-high cable crossover for the upper chest.",
        "Set both cable pulleys to a low position and grab a handle in each hand. Step forward to create tension. Pull the handles up and together in an arc. Squeeze your upper chest at the top and return slowly."
    ),
    Exercise(
        14,
        "Cable Crossover (Mid Height)",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Mid-height cable crossover for overall chest development.",
        "Set both cable pulleys to chest height and grab a handle in each hand. Step forward with your arms out wide. Pull the handles together in an arc across your chest. Squeeze your chest and return slowly under control."
    ),
    Exercise(
        15,
        "Pec Deck Machine",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Pec deck for an isolated chest fly movement.",
        "Sit with your back flat against the pad. Place your forearms or grip the handles. Bring your arms together in front of your chest. Squeeze your chest and return slowly to the start."
    ),
    Exercise(
        16,
        "Seated Chest Press Machine",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Seated chest press machine for a guided pressing movement.",
        "Sit with your back flat against the pad and grip the handles. Plant your feet firmly on the floor. Press the handles forward until your arms are extended. Return them under control to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        17,
        "Incline Chest Press Machine",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Incline chest press machine for upper chest focus.",
        "Adjust the seat so the handles align with your upper chest. Grip the handles and brace your back against the pad. Press the handles upward and forward until your arms are extended. Return slowly to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        18,
        "Decline Chest Press Machine",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Decline chest press machine for lower chest emphasis.",
        "Adjust the seat so the handles align with your lower chest. Grip the handles and keep your back against the pad. Press the handles down and forward until your arms are extended. Return slowly under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        19,
        "Machine Leverage Incline Chest Press",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Plate-loaded incline chest press targeting the upper pectorals.",
        "Sit with your back supported and grip the handles. Adjust the seat so the handles sit at upper chest level. Press the handles forward and upward along the guided path. Return them under control to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        20,
        "Machine Lever Incline Chest Press",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Lever-based angled chest press machine targeting the upper chest.",
        "Adjust the seat and sit with your chest supported. Grip the handles firmly. Press the bar forward and upward along the guided path. Return slowly to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        21,
        "Standard Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Classic push-up for the chest, shoulders, and triceps.",
        "Start in a plank position with hands under your shoulders. Keep your body in a straight line. Lower your chest toward the ground by bending your elbows. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        22,
        "Wide Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Wide hand placement push-up for chest emphasis.",
        "Start in a plank with your hands placed wider than your shoulders. Keep your body in a straight line. Lower your chest toward the ground. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        23,
        "Diamond Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Close hand push-up targeting the inner chest and triceps.",
        "Start in a plank and place your hands together forming a diamond shape. Keep your body straight and core braced. Lower your chest toward your hands. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        24,
        "Decline Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Feet-elevated push-up for upper chest focus.",
        "Place your feet on an elevated surface such as a bench. Set your hands on the floor under your shoulders. Lower your chest toward the ground with your body angled down. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        25,
        "Incline Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Hands-elevated push-up for beginners or lower chest.",
        "Place your hands on an elevated surface such as a bench. Keep your body in a straight line. Lower your chest toward the edge of the surface. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        26,
        "Plyometric Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Explosive push-up with a clap at the top for power.",
        "Start in a plank position with your body straight. Lower your chest toward the ground under control. Explode upward and clap your hands together. Land softly and absorb the impact into the next rep.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        27,
        "Chest Dip (Parallel Bars)",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Forward-leaning dip emphasizing the chest muscles.",
        "Grip the parallel bars and support yourself with straight arms. Lean your torso forward. Lower your body by bending your elbows until you feel a chest stretch. Press back up to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        28,
        "Ring Dip",
        MuscleGroup.CHEST,
        Equipment.SUSPENSION,
        "Dip on gymnastic rings for a stability challenge.",
        "Grip the rings and support yourself with straight arms. Keep the rings stable and close to your body. Lower yourself under control by bending your elbows. Press back up while stabilizing the rings.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        29,
        "Bench Dip",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Dip using a bench for support, a chest and triceps variation.",
        "Place your hands on the edge of a bench behind you. Extend your legs out in front. Lower your body by bending your elbows. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        30,
        "Smith Machine Bench Press",
        MuscleGroup.CHEST,
        Equipment.SMITH_MACHINE,
        "Guided barbell bench press on the Smith machine.",
        "Set the bar at chest height and lie flat on the bench. Unrack the bar by rotating your wrists. Lower it under control to your mid-chest. Press it back up along the guided path.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        31,
        "Smith Machine Incline Press",
        MuscleGroup.CHEST,
        Equipment.SMITH_MACHINE,
        "Incline bench press on the Smith machine.",
        "Set an incline bench under the Smith machine bar. Unrack the bar and hold it over your upper chest. Lower it under control to your upper chest. Press it back up along the guided path.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        32,
        "Smith Machine Decline Press",
        MuscleGroup.CHEST,
        Equipment.SMITH_MACHINE,
        "Decline bench press on the Smith machine.",
        "Set a decline bench under the Smith machine bar. Unrack the bar and hold it over your lower chest. Lower it under control to your lower chest. Press it back up along the guided path.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        33,
        "Landmine Press (Single Arm)",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Single-arm landmine press for unilateral chest work.",
        "Set one end of the barbell in a landmine attachment. Hold the free end in one hand at shoulder level. Press the bar forward and up until your arm is extended. Lower it back to your shoulder under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        34,
        "Landmine Press (Two Arms)",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Two-arm landmine press for chest development.",
        "Set one end of the barbell in a landmine attachment. Hold the free end with both hands at chest level. Press the bar forward and up until your arms are extended. Lower it back to your chest under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        35,
        "Floor Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Bench press from the floor limiting the range of motion.",
        "Lie on the floor with the barbell held over your chest. Bend your knees for stability. Lower the bar until your triceps touch the floor. Press it back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        36,
        "Cable Standing Chest Press",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Standing cable press for functional chest strength.",
        "Set both cable pulleys to chest height and grip a handle in each hand. Stand between the cables with one foot forward. Press the handles forward until your arms are extended. Return them under control to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        37,
        "Cable Standing Chest Fly",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Standing cable fly for chest isolation.",
        "Set both cable pulleys to chest height and grip a handle in each hand. Stand between the cables with your arms out wide. Bring the handles together in an arc in front of your chest. Squeeze your chest and return slowly."
    ),
    Exercise(
        38,
        "One-Arm Push-Up",
        MuscleGroup.CHEST,
        Equipment.BODYWEIGHT,
        "Advanced unilateral push-up for maximum chest engagement.",
        "Place one hand on the floor and the other behind your back. Set your feet in a wide stance for balance. Lower your chest toward the ground on the working arm. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        39,
        "Pull-Up (Overhand)",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Classic overhand pull-up for lat development.",
        "Hang from the bar with an overhand grip slightly wider than shoulders. Engage your back and pull your chin over the bar. Squeeze your shoulder blades at the top. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    ),
    Exercise(
        40,
        "Chin-Up (Underhand)",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Underhand chin-up targeting the lats and biceps.",
        "Hang from the bar with an underhand grip about shoulder width. Pull your chin over the bar by driving your elbows down. Squeeze at the top of the movement. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    ),
    Exercise(
        41,
        "Neutral Grip Pull-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Neutral grip pull-up using parallel handles.",
        "Grip the parallel handles with your palms facing each other. Hang with your arms fully extended. Pull your chin above the handles by driving your elbows down. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    ),
    Exercise(
        42,
        "Muscle-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Explosive pull-up transitioning into a dip above the bar.",
        "Hang from the bar with a firm overhand grip. Pull explosively while leaning into the bar. Transition your wrists over the bar as you rise. Press up until your arms are straight above the bar.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        43,
        "Weighted Pull-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Pull-up with added weight for strength progression.",
        "Attach a weight belt or hold a dumbbell between your feet. Hang from the bar with an overhand grip. Pull your chin over the bar with control. Lower yourself slowly to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    ),
    Exercise(
        44,
        "Assisted Pull-Up (Machine)",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Machine-assisted pull-up that helps beginners build pulling strength.",
        "Set the assistance weight to match your needs. Place your knees or feet on the support pad. Grip the handles with an overhand grip. Pull your chin over the bar while squeezing your lats. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        45,
        "Assisted Pull-Up (Band)",
        MuscleGroup.BACK,
        Equipment.RESISTANCE_BAND,
        "Band-assisted pull-up for progressive bodyweight training.",
        "Loop a resistance band around the bar and pull it through itself. Place one foot or knee into the bottom of the band. Grip the bar with an overhand grip. Pull your chin over the bar using the band for assistance. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        46,
        "Barbell Deadlift",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Classic deadlift for overall posterior chain development.",
        "Stand with your feet hip-width apart and the bar over your midfoot. Hinge at the hips and grip the bar just outside your knees. Brace your core and keep your back flat. Drive through your heels and stand up tall, locking out the hips. Lower the bar under control back to the floor.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK, MuscleGroup.TRAPS)
    ),
    Exercise(
        47,
        "Barbell Romanian Deadlift",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Romanian deadlift targeting the hamstrings and lower back.",
        "Hold the barbell at your hips with an overhand grip. Keep a slight bend in your knees throughout. Hinge at the hips and push them back as the bar travels down your legs. Lower until you feel a stretch in your hamstrings. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        48,
        "Barbell Sumo Deadlift",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Wide stance deadlift that reduces lower back stress.",
        "Stand with a wide stance and your toes pointed out. Grip the bar with your hands inside your knees. Brace your core and keep your chest up. Drive through your heels and push your knees out as you stand. Lock out your hips at the top and lower under control.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK, MuscleGroup.QUADS)
    ),
    Exercise(
        49,
        "Trap Bar Deadlift",
        MuscleGroup.BACK,
        Equipment.TRAP_BAR,
        "Deadlift using a trap bar for a more upright torso.",
        "Step into the center of the trap bar with feet hip-width apart. Hinge down and grip the handles at your sides. Brace your core and keep your chest up. Drive through your heels to stand up tall. Lower the bar under control to the floor.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS, MuscleGroup.TRAPS)
    ),
    Exercise(
        50,
        "Dumbbell Romanian Deadlift",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Romanian deadlift with dumbbells for the hamstrings and back.",
        "Hold a dumbbell in each hand in front of your thighs. Keep a slight bend in your knees. Hinge at the hips and lower the weights close to your legs. Stop when you feel a stretch in your hamstrings. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        51,
        "Kettlebell Deadlift",
        MuscleGroup.BACK,
        Equipment.KETTLEBELL,
        "Deadlift using a kettlebell to practice the hinge pattern.",
        "Stand over the kettlebell with feet hip-width apart. Hinge at the hips and grip the handle with both hands. Brace your core and keep your back flat. Drive through your heels and stand up tall. Lower the kettlebell under control back to the floor.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        52,
        "Barbell Bent-Over Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Bent-over barbell row for building upper back thickness.",
        "Hinge at the hips until your torso is near parallel to the floor. Let the bar hang with an overhand grip. Pull the barbell toward your lower chest. Squeeze your shoulder blades together at the top. Lower the bar under control to full extension.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        53,
        "Barbell Pendlay Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Explosive row from a dead stop on the floor each rep.",
        "Hinge until your torso is parallel to the floor. Grip the bar with an overhand grip just outside your knees. Pull the bar explosively to your lower chest. Lower the bar back to the floor between each rep. Reset your position before the next pull.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        54,
        "Barbell Seal Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Chest-supported barbell row that eliminates lower back involvement.",
        "Lie face down on an elevated bench with the barbell below you. Reach down and grip the bar with an overhand grip. Pull the bar up toward the bench. Squeeze your shoulder blades together at the top. Lower the bar under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        55,
        "Dumbbell Seal Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Chest-supported dumbbell row that eliminates lower back involvement.",
        "Lie face down on an elevated bench with dumbbells below you. Grip a dumbbell in each hand with a neutral grip. Pull the dumbbells up toward the bench. Squeeze your shoulder blades together at the top. Lower the weights under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        56,
        "Dumbbell Single-Arm Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Single-arm dumbbell row for unilateral lat development.",
        "Place one knee and hand on a bench for support. Hold a dumbbell in the opposite hand with a neutral grip. Pull the dumbbell toward your hip keeping your elbow close to your body. Squeeze your lat at the top. Lower the weight under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        57,
        "Dumbbell Chest-Supported Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Incline chest-supported row that removes momentum and cheating.",
        "Lie face down on an incline bench set to a moderate angle. Hold a dumbbell in each hand hanging toward the floor. Pull the dumbbells up toward your hips. Squeeze your shoulder blades together at the top. Lower the weights under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        58,
        "Dumbbell Renegade Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Row from a push-up position for core and back stability.",
        "Start in a push-up position gripping a dumbbell in each hand. Keep your hips level and your core braced. Row one dumbbell up toward your hip. Lower it under control and repeat on the other side. Avoid rotating your torso throughout the movement.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.ABS)
    ),
    Exercise(
        59,
        "Cable Seated Row",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Seated cable row for mid-back development.",
        "Sit with your feet braced on the platform. Grip the handle with a slight bend in your knees. Pull the handle toward your abdomen. Squeeze your shoulder blades together at the top. Extend your arms under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        60,
        "Cable Seated Row (Wide Grip)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Wide grip seated cable row emphasizing the upper back.",
        "Sit and grip a wide bar attachment with an overhand grip. Brace your feet on the platform. Pull the bar toward your upper chest. Squeeze your shoulder blades together at the top. Extend your arms under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        61,
        "Cable Straight-Arm Pulldown",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Straight-arm pulldown for lat isolation.",
        "Stand facing the cable with the bar set high. Grip the bar with straight arms and a slight forward lean. Keeping your arms straight, pull the bar down to your thighs. Squeeze your lats at the bottom. Return the bar under control to the starting position."
    ),
    Exercise(
        62,
        "Lat Pulldown (Wide Grip)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Wide grip lat pulldown for building lat width.",
        "Grip the bar wider than shoulder width. Secure your thighs under the pads. Pull the bar to your upper chest while squeezing your lats. Keep your chest up and avoid leaning back excessively. Return the bar under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        63,
        "Lat Pulldown (Close Grip)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Close grip lat pulldown emphasizing the mid-back.",
        "Attach a close grip handle and secure your thighs under the pads. Grip the handles with a neutral grip. Pull the handle to your chest keeping your elbows close to your body. Squeeze your back at the bottom. Return the handle under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        64,
        "Lat Pulldown (Underhand)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Underhand lat pulldown emphasizing the lower lats and biceps.",
        "Grip the bar with an underhand grip at shoulder width. Secure your thighs under the pads. Pull the bar to your upper chest. Squeeze your lats at the bottom. Return the bar under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        65,
        "Lat Pulldown (Neutral Grip)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Neutral grip lat pulldown using parallel handles.",
        "Attach a parallel grip handle and secure your thighs under the pads. Grip the handles with palms facing each other. Pull the handle down toward your chest. Squeeze your lats at the bottom. Return the handle under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        66,
        "Single-Arm Lat Pulldown",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Unilateral lat pulldown for balanced back development.",
        "Sit or kneel facing the cable with a single handle attached high. Grip the handle with one hand. Pull the handle down toward your shoulder. Squeeze your lat at the bottom. Return the handle under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        67,
        "Machine Lying Reverse T-Bar Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Lying chest-supported T-bar row with an underhand grip.",
        "Lie prone on the angled pad with your chest supported. Grasp the handles with a reverse underhand grip. Pull the handles toward your lower chest. Squeeze your shoulder blades together at the top. Lower the weight under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        68,
        "Machine Lying T-Bar Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Prone chest-supported T-bar row for mid-back development.",
        "Lie prone on the angled pad with your chest supported. Grasp the handles with an overhand grip. Pull the handles toward your upper chest. Squeeze your shoulder blades together at the top. Lower the weight under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        69,
        "Face Pull",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Face pull for rear delts and upper back health.",
        "Set a rope attachment at about head height. Grip the rope ends with thumbs pointing back. Pull the rope toward your face flaring your elbows out. Externally rotate your hands at the top. Return the rope under control to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS)
    ),
    Exercise(
        70,
        "Cable Rear Delt Row",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Cable row targeting the rear deltoids and upper back.",
        "Set the cables at chest height and grip the handles crossed. Step back to create tension. Pull the handles back keeping your arms out to your sides. Squeeze your rear delts at the top. Return the handles under control to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS)
    ),
    Exercise(
        71,
        "Machine Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Seated machine row for guided back training.",
        "Sit with your chest against the support pad. Grip the handles with a comfortable grip. Pull the handles back toward your torso. Squeeze your shoulder blades together at the top. Return the handles under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        72,
        "Machine Lat Pulldown",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Machine lat pulldown with a guided motion.",
        "Sit and secure your thighs under the pads. Grip the handles overhead. Pull the handles down toward your chest. Squeeze your lats at the bottom. Return the handles under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        73,
        "Machine High Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "High row machine for upper back development.",
        "Sit with your chest against the support pad. Grip the handles in the high starting position. Pull the handles down and back toward your torso. Squeeze your shoulder blades together at the bottom. Return the handles under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        74,
        "Reverse Grip Machine Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Machine row performed with an underhand grip.",
        "Sit with your chest against the support pad. Grasp the handles with a reverse underhand grip. Pull the handles back toward your waist. Squeeze your shoulder blades together at the top. Return the handles under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        75,
        "Inverted Row",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Bodyweight row under a fixed bar for back development.",
        "Set a bar at about waist height and hang underneath it. Grip the bar with an overhand grip and keep your body straight. Pull your chest up to the bar. Squeeze your shoulder blades together at the top. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        76,
        "Inverted Row (Elevated Feet)",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Inverted row with feet elevated for increased difficulty.",
        "Set a bar at waist height and place your feet on a bench. Grip the bar with an overhand grip and keep your body horizontal. Pull your chest up to the bar. Squeeze your shoulder blades together at the top. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        77,
        "Ring Row",
        MuscleGroup.BACK,
        Equipment.SUSPENSION,
        "Bodyweight row using gymnastic rings.",
        "Grip the rings and lean back with your body straight. Keep your core braced throughout. Pull your chest up to the handles while controlling the rings. Squeeze your shoulder blades together at the top. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        78,
        "Ring Elevated Row",
        MuscleGroup.BACK,
        Equipment.SUSPENSION,
        "Bodyweight ring row with feet elevated for added difficulty.",
        "Place your feet on an elevated surface and grip the rings. Keep your body horizontal and your core braced. Pull your chest up to the handles while controlling the rings. Squeeze your shoulder blades together at the top. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        79,
        "Smith Machine Row",
        MuscleGroup.BACK,
        Equipment.SMITH_MACHINE,
        "Bent-over row performed on the Smith machine.",
        "Hinge at the hips with your torso near parallel to the floor. Grip the bar with an overhand grip. Pull the bar to your abdomen along the guided path. Squeeze your shoulder blades together at the top. Lower the bar under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        80,
        "Cable One-Arm Row",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Single-arm cable row for unilateral back work.",
        "Sit or stand facing the cable with a single handle attached. Grip the handle with one hand. Pull the handle toward the side of your torso. Squeeze your lat at the top. Extend your arm under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        81,
        "Resistance Band Row",
        MuscleGroup.BACK,
        Equipment.RESISTANCE_BAND,
        "Seated row using a resistance band.",
        "Sit with your legs extended and loop the band around your feet. Grip the band ends with both hands. Pull the handles toward your abdomen. Squeeze your shoulder blades together at the top. Return the band under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        82,
        "Resistance Band Lat Pulldown",
        MuscleGroup.BACK,
        Equipment.RESISTANCE_BAND,
        "Lat pulldown using a resistance band.",
        "Anchor a resistance band securely overhead. Grip the band with both hands and kneel or sit below it. Pull the band down toward your chest. Squeeze your lats at the bottom. Return the band under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        83,
        "Resistance Band Face Pull",
        MuscleGroup.BACK,
        Equipment.RESISTANCE_BAND,
        "Face pull using a resistance band.",
        "Anchor a resistance band at about face height. Grip the band with both hands and step back for tension. Pull the band toward your face while flaring your elbows. Externally rotate your hands at the top. Return the band under control to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS)
    ),
    Exercise(
        84,
        "Kettlebell Row (Single Arm)",
        MuscleGroup.BACK,
        Equipment.KETTLEBELL,
        "Single-arm kettlebell row for unilateral back development.",
        "Hinge at the hips with a flat back and brace one hand on your thigh or a bench. Grip the kettlebell with the other hand. Pull the kettlebell toward your hip keeping your elbow close. Squeeze your lat at the top. Lower the kettlebell under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        85,
        "Negative Pull-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Slow eccentric pull-up for building pulling strength.",
        "Jump or step up to the top position with your chin over the bar. Grip the bar with an overhand grip. Lower yourself as slowly as possible. Maintain control through the entire descent. Reset to the top and repeat.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        86,
        "Cable Pullover (Straight Arm)",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Straight-arm cable pullover for a strong lat stretch and contraction.",
        "Stand facing the cable with the bar set high. Grip the bar with straight arms. Pull the bar in an arc from overhead down to your thighs. Squeeze your lats at the bottom. Return the bar under control to a full stretch."
    ),
    Exercise(
        87,
        "Machine Pullover",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Machine pullover that isolates the lats through a wide arc.",
        "Sit with your chest against the pad and grip the handles overhead. Brace your core and keep your torso upright. Pull the handles down and forward in a smooth arc. Squeeze your lats at the bottom and return under control.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)
    ),
    Exercise(
        88,
        "Barbell Overhead Press",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Standing barbell press for overall shoulder development.",
        "Stand with the bar racked at shoulder height and feet shoulder width apart. Brace your core and squeeze your glutes. Press the bar straight overhead to full lockout. Lower it back to your shoulders under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        89,
        "Barbell Seated Overhead Press",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Seated barbell overhead press with reduced lower body involvement.",
        "Sit on a bench with back support and the bar at shoulder height. Brace your core and keep your back against the pad. Press the bar straight overhead to lockout. Lower it back to your shoulders with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        90,
        "Barbell Push Press",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Overhead press using leg drive to move heavier loads.",
        "Stand with the bar racked at shoulder height. Dip slightly by bending your knees. Drive up explosively through your legs and press the bar overhead. Lower it back to your shoulders under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.QUADS)
    ),
    Exercise(
        91,
        "Dumbbell Shoulder Press",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Seated dumbbell press for balanced shoulder development.",
        "Sit on a bench with back support and dumbbells at shoulder height. Brace your core and keep your wrists stacked. Press the dumbbells overhead until your arms are extended. Lower them back to shoulder height with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        92,
        "Dumbbell Arnold Press",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Rotating dumbbell press that hits all three deltoid heads.",
        "Sit with dumbbells in front of your shoulders and palms facing you. Rotate your palms outward as you press the weights overhead. Extend your arms fully at the top. Reverse the rotation as you lower back to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        93,
        "Dumbbell Lateral Raise",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Dumbbell lateral raise targeting the medial deltoid.",
        "Stand with a dumbbell in each hand at your sides. Keep a slight bend in your elbows. Raise the dumbbells out to the sides up to shoulder height. Lower them back down slowly."
    ),
    Exercise(
        94,
        "Dumbbell Front Raise",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Dumbbell front raise targeting the anterior deltoid.",
        "Stand with dumbbells resting in front of your thighs. Keep a slight bend in your elbows. Raise the dumbbells forward up to shoulder height. Lower them back down under control."
    ),
    Exercise(
        95,
        "Dumbbell Rear Delt Fly",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Bent-over fly that isolates the posterior deltoids.",
        "Hinge forward at your hips with dumbbells hanging below your chest. Keep a slight bend in your elbows and a flat back. Raise the dumbbells out to the sides while squeezing your rear delts. Lower them back down slowly."
    ),
    Exercise(
        96,
        "Cable Lateral Raise",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable lateral raise for constant tension on the medial delts.",
        "Stand sideways to a low cable and grip the handle across your body. Keep a slight bend in your elbow. Raise the handle out to the side up to shoulder height. Lower it back down with control."
    ),
    Exercise(
        97,
        "Cable Front Raise",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable front raise targeting the anterior deltoid.",
        "Stand facing away from a low cable with the handle between your legs. Keep a slight bend in your elbow. Raise the handle forward up to shoulder height. Lower it back down under control."
    ),
    Exercise(
        98,
        "Cable Rear Delt Fly",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable reverse fly that isolates the posterior deltoids.",
        "Set two cables at shoulder height and grab the opposite handles so the cables cross in front of you. Keep your arms slightly bent. Pull the handles out and back while squeezing your rear delts. Return to the start under control."
    ),
    Exercise(
        99,
        "Cable Upright Row",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable upright row working the lateral delts and traps.",
        "Stand facing a low cable holding a straight bar attachment. Pull the bar up along your body leading with your elbows. Bring it to chest height while keeping elbows above your wrists. Lower it back down with control.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS, MuscleGroup.BICEPS)
    ),
    Exercise(
        100,
        "Cable Face Pull",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable face pull for rear delts and rotator cuff health.",
        "Set a rope attachment at upper chest height and grip both ends. Pull the rope toward your face while flaring your elbows out. Externally rotate your hands at the end of the pull. Return to the start under control.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS)
    ),
    Exercise(
        101,
        "Machine Shoulder Press",
        MuscleGroup.SHOULDERS,
        Equipment.MACHINE,
        "Seated machine shoulder press along a guided path.",
        "Sit with your back against the pad and grip the handles. Brace your core and keep your feet planted. Press the handles overhead until your arms are extended. Lower them back to shoulder height with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        102,
        "Machine Lateral Raise",
        MuscleGroup.SHOULDERS,
        Equipment.MACHINE,
        "Machine lateral raise that isolates the medial delts.",
        "Sit with your upper arms against the pads. Keep your torso upright and still. Raise your arms out to the sides against the resistance. Lower them back down slowly."
    ),
    Exercise(
        103,
        "Machine Rear Delt Fly",
        MuscleGroup.SHOULDERS,
        Equipment.MACHINE,
        "Machine reverse fly isolating the rear deltoids.",
        "Sit facing the pad and grip the handles at shoulder height. Keep your arms slightly bent. Push the handles back and out while squeezing your rear delts. Return to the start under control."
    ),
    Exercise(
        104,
        "Smith Machine Shoulder Press",
        MuscleGroup.SHOULDERS,
        Equipment.SMITH_MACHINE,
        "Shoulder press on the Smith machine for added stability.",
        "Sit on a bench positioned under the Smith machine bar. Grip the bar at shoulder height with hands slightly wider than shoulders. Press the bar straight up along the guided path to lockout. Lower it back to your shoulders under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        105,
        "Kettlebell Shoulder Press",
        MuscleGroup.SHOULDERS,
        Equipment.KETTLEBELL,
        "Kettlebell overhead press for shoulder strength and stability.",
        "Hold a kettlebell in the racked position at your shoulder. Brace your core and keep your wrist straight. Press the kettlebell overhead until your arm is extended. Lower it back to the rack with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        106,
        "Resistance Band Pull-Apart",
        MuscleGroup.SHOULDERS,
        Equipment.RESISTANCE_BAND,
        "Band pull-apart for rear delts and upper back.",
        "Hold a resistance band in front of you at shoulder height with both hands. Keep your arms straight. Pull the band apart while squeezing your shoulder blades together. Return to the start slowly.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS)
    ),
    Exercise(
        107,
        "Pike Push-Up",
        MuscleGroup.SHOULDERS,
        Equipment.BODYWEIGHT,
        "Pike push-up that loads the shoulders with bodyweight.",
        "Start in a downward dog position with hips high in an inverted V. Keep your arms straight and head between your hands. Bend your elbows to lower your head toward the ground. Press back up to the starting position.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        108,
        "Handstand Push-Up",
        MuscleGroup.SHOULDERS,
        Equipment.BODYWEIGHT,
        "Advanced overhead push using full bodyweight against a wall.",
        "Kick up into a handstand with your heels resting against a wall. Brace your core and keep your body straight. Bend your elbows to lower your head toward the floor. Press back up to full arm extension.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        109,
        "Dumbbell Y-Raise",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Y-raise targeting the lower traps and rear delts.",
        "Hinge forward at your hips with dumbbells hanging down. Keep your arms mostly straight. Raise the dumbbells diagonally up and out to form a Y shape. Lower them back down with control."
    ),
    Exercise(
        110,
        "Cable External Rotation",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable external rotation for rotator cuff health.",
        "Stand sideways to the cable with your elbow tucked at your side. Bend your elbow to ninety degrees holding the handle across your body. Rotate your forearm outward away from your body against the resistance. Return to the start slowly."
    ),
    Exercise(
        111,
        "Cable Internal Rotation",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Cable internal rotation for rotator cuff strengthening.",
        "Stand sideways to the cable with your elbow tucked at your side. Bend your elbow to ninety degrees holding the handle out from your body. Rotate your forearm inward across your body against the resistance. Return to the start under control."
    ),
    Exercise(
        112,
        "Landmine Press (Single Arm)",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Single-arm landmine press for the shoulders at an angle.",
        "Hold the end of a landmine-loaded bar at your shoulder with one hand. Brace your core and stagger your stance. Press the bar up and forward until your arm is extended. Lower it back to your shoulder with control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        113,
        "Z-Press",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Seated floor overhead press demanding core and shoulder strength.",
        "Sit on the floor with your legs straight out in front of you. Hold the bar at shoulder height and brace your core hard. Press the bar straight overhead to lockout. Lower it back to your shoulders under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS)
    ),
    Exercise(
        114,
        "Dumbbell Scaption Raise",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Raise in the scapular plane for shoulder health.",
        "Stand with dumbbells in front of your thighs and palms facing each other. Keep a slight bend in your elbows. Raise the dumbbells up and out at about a thirty degree angle to shoulder height. Lower them back down slowly."
    ),
    Exercise(
        115,
        "Prone Y-T-W Raise",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Combination raise for complete rear shoulder and upper back work.",
        "Lie face down on an incline bench or the floor with light dumbbells. Raise your arms into a Y shape overhead. Lower and then raise them out to the sides into a T shape. Finally pull your elbows back into a W shape before lowering.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS, MuscleGroup.BACK)
    ),
    Exercise(
        116,
        "Dumbbell Cuban Press",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Cuban press combining rotation and press for shoulder health.",
        "Hold dumbbells with a neutral grip and elbows bent at your sides. Externally rotate your forearms upward to shoulder height. Press the dumbbells overhead until your arms extend. Reverse the sequence to return to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        117,
        "Kettlebell Single-Arm Press",
        MuscleGroup.SHOULDERS,
        Equipment.KETTLEBELL,
        "Single-arm kettlebell overhead press for unilateral strength.",
        "Hold a kettlebell in the racked position at one shoulder. Brace your core to resist leaning. Press the kettlebell overhead until your arm is extended. Lower it back to the rack and alternate arms.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS)
    ),
    Exercise(
        118,
        "Barbell Curl",
        MuscleGroup.BICEPS,
        Equipment.BARBELL,
        "Classic standing barbell curl for bicep mass.",
        "Stand with the barbell held at your waist with an underhand grip. Keep your elbows tucked at your sides. Curl the bar up while squeezing your biceps. Lower it back down under control."
    ),
    Exercise(
        119,
        "EZ-Bar Curl",
        MuscleGroup.BICEPS,
        Equipment.EZ_BAR,
        "EZ-bar curl for a more comfortable wrist position.",
        "Stand holding the EZ-bar by the angled inner grips. Keep your elbows pinned at your sides. Curl the bar up while squeezing your biceps. Lower it back down slowly."
    ),
    Exercise(
        120,
        "Dumbbell Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Standard dumbbell curl for bicep development.",
        "Stand with a dumbbell in each hand at your sides and palms forward. Keep your elbows tucked. Curl the dumbbells up while squeezing your biceps. Lower them back down with control."
    ),
    Exercise(
        121,
        "Dumbbell Hammer Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Hammer curl for the brachialis and forearms.",
        "Stand with dumbbells at your sides using a neutral grip. Keep your palms facing each other throughout. Curl the dumbbells up while keeping your elbows tucked. Lower them back down slowly."
    ),
    Exercise(
        122,
        "Dumbbell Incline Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Incline curl that stretches the bicep long head.",
        "Sit back on an incline bench with dumbbells in each hand. Let your arms hang straight down behind your torso. Curl the dumbbells up from the stretched position while squeezing your biceps. Lower them back down under control."
    ),
    Exercise(
        123,
        "Dumbbell Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Preacher curl with a dumbbell for strict bicep isolation.",
        "Rest your upper arm on the preacher pad holding a dumbbell. Let your arm extend down the pad. Curl the dumbbell up while squeezing your bicep. Lower it back to the stretched position slowly."
    ),
    Exercise(
        124,
        "Dumbbell Concentration Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Concentration curl for an isolated bicep peak contraction.",
        "Sit on a bench and brace your working elbow against your inner thigh. Let the dumbbell hang at arm length. Curl it up while squeezing your bicep peak. Lower it back down with control."
    ),
    Exercise(
        125,
        "Dumbbell Zottman Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Zottman curl with a pronated lowering phase for the forearms.",
        "Stand with dumbbells at your sides and palms facing forward. Curl the dumbbells up with a supinated grip. Rotate your palms to face down at the top. Lower the dumbbells with the overhand grip and rotate back at the bottom."
    ),
    Exercise(
        126,
        "Cable Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Cable bicep curl for constant tension through the range.",
        "Stand facing a low cable holding the bar or handle. Keep your elbows tucked at your sides. Curl the attachment up while squeezing your biceps. Lower it back down slowly."
    ),
    Exercise(
        127,
        "Cable Rope Hammer Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Rope hammer curl on the cable for the brachialis.",
        "Stand facing a low cable holding a rope attachment with a neutral grip. Keep your palms facing each other and elbows tucked. Curl the rope up while squeezing your biceps. Lower it back down with control."
    ),
    Exercise(
        128,
        "Cable Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Preacher curl using the cable for constant tension.",
        "Rest your upper arms on the preacher pad and grip the cable handle. Let your arms extend down the pad. Curl the handle up while squeezing your biceps. Lower it back to the stretched position under control."
    ),
    Exercise(
        129,
        "Machine Bicep Curl",
        MuscleGroup.BICEPS,
        Equipment.MACHINE,
        "Machine bicep curl for a guided isolation movement.",
        "Sit with your upper arms resting on the pad and grip the handles. Keep your torso upright. Curl the handles up while squeezing your biceps. Lower them back down following the machine path."
    ),
    Exercise(
        130,
        "Machine Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.MACHINE,
        "Machine preacher curl with arm support for isolated bicep work.",
        "Sit and rest the backs of your arms flat against the pad. Grip the handles with palms facing up. Curl the handles up by squeezing your biceps. Lower under control back to the stretched position."
    ),
    Exercise(
        131,
        "Chin-Up",
        MuscleGroup.BICEPS,
        Equipment.BODYWEIGHT,
        "Underhand-grip pull-up that emphasizes the biceps and back.",
        "Grip the bar with palms facing you about shoulder width apart. Hang with arms fully extended and core braced. Pull your chin up over the bar by driving your elbows down. Lower yourself under control to a full hang.",
        secondaryMuscles = listOf(MuscleGroup.BACK)
    ),
    Exercise(
        132,
        "Barbell Reverse Curl",
        MuscleGroup.BICEPS,
        Equipment.BARBELL,
        "Overhand-grip barbell curl that targets the brachialis and forearm extensors.",
        "Hold the barbell with an overhand grip at shoulder width. Keep your elbows tucked at your sides. Curl the bar up while keeping your wrists firm. Lower the bar slowly back to the starting position."
    ),
    Exercise(
        133,
        "Kettlebell Curl",
        MuscleGroup.BICEPS,
        Equipment.KETTLEBELL,
        "Bicep curl performed with a kettlebell.",
        "Hold the kettlebell by the handle with your palm facing up. Keep your elbow pinned to your side. Curl the kettlebell up by squeezing your bicep. Lower it slowly back down to full extension."
    ),
    Exercise(
        134,
        "EZ-Bar Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.EZ_BAR,
        "Preacher curl using an EZ-bar for improved wrist comfort.",
        "Rest the backs of your arms on the preacher pad. Grip the EZ-bar at its angled sections. Curl the bar up while squeezing your biceps. Lower it under control to a full stretch."
    ),
    Exercise(
        135,
        "Tricep Pushdown (Straight Bar)",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Straight-bar cable pushdown for tricep development.",
        "Stand facing the cable with the bar set at chest height. Grip the bar with palms facing down and elbows tucked. Push the bar down until your arms are fully extended. Squeeze your triceps, then return with control."
    ),
    Exercise(
        136,
        "Tricep Pushdown (Rope)",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Rope cable pushdown for strong tricep peak contraction.",
        "Stand facing the cable and grip the rope with both hands. Keep your elbows tucked close to your sides. Push down and spread the rope apart at the bottom. Squeeze your triceps, then return slowly."
    ),
    Exercise(
        137,
        "Single-Arm Tricep Pushdown",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Single-arm cable pushdown for unilateral tricep work.",
        "Stand facing the cable and grip the handle with one hand. Keep that elbow tucked against your side. Push the handle down until the arm is fully extended. Squeeze the tricep, then return with control."
    ),
    Exercise(
        138,
        "Overhead Tricep Extension (Cable)",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Overhead cable extension that emphasizes the tricep long head.",
        "Face away from the cable with the rope set low. Hold the rope overhead with elbows bent. Extend your arms forward and up until straight. Squeeze your triceps, then lower under control."
    ),
    Exercise(
        139,
        "Barbell Lying Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Lying barbell skull crusher for tricep mass.",
        "Lie flat on a bench holding the barbell above your chest. Keep your upper arms vertical and fixed. Bend your elbows to lower the bar toward your forehead. Extend your arms to press the bar back up."
    ),
    Exercise(
        140,
        "EZ-Bar Lying Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "EZ-bar skull crusher for tricep work with wrist comfort.",
        "Lie flat on a bench holding the EZ-bar above your chest. Keep your upper arms steady and pointing up. Lower the bar toward your forehead by bending your elbows. Extend your arms back up to the start."
    ),
    Exercise(
        141,
        "Dumbbell Lying Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.DUMBBELL,
        "Lying dumbbell skull crusher allowing independent arm work.",
        "Lie flat on a bench holding a dumbbell in each hand above your chest. Keep your upper arms fixed and vertical. Lower the dumbbells toward the sides of your head. Extend your arms back up to the top."
    ),
    Exercise(
        142,
        "Dumbbell Overhead Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.DUMBBELL,
        "Seated overhead dumbbell extension for the tricep long head.",
        "Sit upright holding a dumbbell overhead with both hands. Keep your elbows close to your head. Lower the dumbbell behind your head by bending your elbows. Extend your arms to press it back overhead."
    ),
    Exercise(
        143,
        "Dumbbell Kickback",
        MuscleGroup.TRICEPS,
        Equipment.DUMBBELL,
        "Bent-over dumbbell kickback for tricep isolation.",
        "Hinge forward at the hips with a flat back. Hold a dumbbell with your upper arm parallel to your torso. Extend your arm straight back until fully locked out. Squeeze the tricep, then return with control."
    ),
    Exercise(
        144,
        "Close-Grip Bench Press",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Narrow-grip bench press that builds tricep mass.",
        "Lie flat on the bench and grip the bar at shoulder width. Lower the bar to your lower chest keeping elbows close to your body. Press the bar back up to full lockout. Keep your wrists straight throughout.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        145,
        "Diamond Push-Up",
        MuscleGroup.TRICEPS,
        Equipment.BODYWEIGHT,
        "Close-hand push-up that emphasizes the triceps.",
        "Place your hands together under your chest forming a diamond shape. Keep your body in a straight line from head to heels. Lower your chest toward your hands by bending your elbows. Press back up while focusing on your triceps.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        146,
        "Tricep Dip (Parallel Bars)",
        MuscleGroup.TRICEPS,
        Equipment.BODYWEIGHT,
        "Upright parallel-bar dip for tricep development.",
        "Grip the parallel bars and support yourself with arms locked out. Keep your torso upright to bias the triceps. Lower your body by bending your elbows. Press back up to full lockout.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        147,
        "Bench Dip",
        MuscleGroup.TRICEPS,
        Equipment.BODYWEIGHT,
        "Bench dip for tricep isolation using bodyweight.",
        "Place your hands on the edge of a bench behind you. Extend your legs out in front with heels on the floor. Lower your body by bending your elbows. Press back up until your arms are straight."
    ),
    Exercise(
        148,
        "Ring Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.SUSPENSION,
        "Suspension ring extension for the triceps.",
        "Grip the rings and lean forward with arms extended. Keep your body straight and core braced. Bend your elbows to lower your body forward. Extend your arms to push your body back to the start."
    ),
    Exercise(
        149,
        "Machine Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.MACHINE,
        "Machine tricep extension for a guided movement path.",
        "Sit with your arms resting on the pad. Grip the handles with your elbows bent. Extend the handles down by straightening your arms. Squeeze your triceps, then return along the machine path."
    ),
    Exercise(
        150,
        "Resistance Band Pushdown",
        MuscleGroup.TRICEPS,
        Equipment.RESISTANCE_BAND,
        "Tricep pushdown using a resistance band.",
        "Anchor the band securely overhead. Grip the handles with your elbows tucked at your sides. Push the handles down until your arms are fully extended. Squeeze your triceps, then return slowly against the band."
    ),
    Exercise(
        151,
        "Resistance Band Overhead Extension",
        MuscleGroup.TRICEPS,
        Equipment.RESISTANCE_BAND,
        "Overhead tricep extension using a resistance band.",
        "Stand on the band and hold the handles behind your head. Keep your elbows pointed forward and close to your head. Extend your arms overhead against the band resistance. Lower under control back to the start."
    ),
    Exercise(
        152,
        "Kettlebell Overhead Extension",
        MuscleGroup.TRICEPS,
        Equipment.KETTLEBELL,
        "Overhead tricep extension using a kettlebell.",
        "Hold the kettlebell overhead with both hands. Keep your elbows close to your head. Lower the kettlebell behind your head by bending your elbows. Extend your arms to press it back overhead."
    ),
    Exercise(
        153,
        "EZ-Bar Standing Overhead Extension",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Standing overhead tricep extension with an EZ-bar.",
        "Stand or sit upright holding the EZ-bar overhead. Keep your elbows pointing forward and tucked in. Lower the bar behind your head by bending your elbows. Extend your arms to press the bar back up."
    ),
    Exercise(
        154,
        "Dumbbell Tricep Extension (Single Arm)",
        MuscleGroup.TRICEPS,
        Equipment.DUMBBELL,
        "Single-arm lying dumbbell extension for unilateral tricep work.",
        "Lie flat on a bench holding one dumbbell above your shoulder. Keep your upper arm fixed and vertical. Lower the dumbbell beside your head by bending your elbow. Extend your arm to press it back up."
    ),
    Exercise(
        155,
        "Crunch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Basic crunch targeting the upper abs.",
        "Lie on your back with your knees bent and feet flat. Place your hands behind your head or across your chest. Curl your shoulders off the floor by squeezing your abs. Lower back down under control."
    ),
    Exercise(
        156,
        "Crunch (Weighted)",
        MuscleGroup.ABS,
        Equipment.PLATE,
        "Weighted crunch for progressive ab overload.",
        "Lie on your back with your knees bent and feet flat. Hold a weight plate against your chest. Curl your shoulders off the floor against the added resistance. Lower back down under control."
    ),
    Exercise(
        157,
        "Reverse Crunch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Reverse crunch targeting the lower abs.",
        "Lie on your back with your hands at your sides. Bend your knees and bring them over your hips. Curl your hips off the floor by drawing your knees toward your chest. Lower back down under control."
    ),
    Exercise(
        158,
        "Bicycle Crunch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Bicycle crunch targeting the abs and obliques.",
        "Lie on your back with your hands behind your head. Lift your shoulders and legs off the floor. Bring one elbow toward the opposite knee while extending the other leg. Alternate sides in a smooth cycling motion."
    ),
    Exercise(
        159,
        "Oblique Crunch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Side crunch that targets the obliques.",
        "Lie on your back with your knees bent and dropped to one side. Place your hands behind your head. Curl your shoulder up toward your hip to target the obliques. Lower back down and repeat before switching sides."
    ),
    Exercise(
        160,
        "Cable Crunch",
        MuscleGroup.ABS,
        Equipment.CABLE,
        "Kneeling cable crunch for weighted ab contraction.",
        "Kneel facing the cable and hold the rope beside your head. Keep your hips fixed in place. Curl your torso down bringing your elbows toward your thighs. Squeeze your abs, then return under control."
    ),
    Exercise(
        161,
        "Cable Woodchop",
        MuscleGroup.ABS,
        Equipment.CABLE,
        "Cable woodchop for rotational core strength.",
        "Set the cable high and grip the handle with both hands. Stand side-on with your feet shoulder width apart. Pull the cable diagonally across your body from high to low. Rotate through your core, then return with control.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS)
    ),
    Exercise(
        162,
        "Hanging Leg Raise",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Hanging leg raise targeting the lower abs.",
        "Hang from a bar with arms fully extended. Keep your legs straight and core braced. Raise your legs to parallel or higher by curling your pelvis. Lower them slowly without swinging."
    ),
    Exercise(
        163,
        "Hanging Knee Raise",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Hanging knee raise targeting the lower abs.",
        "Hang from a bar with arms fully extended. Keep your core braced and avoid swinging. Raise your knees toward your chest by curling your pelvis. Lower them slowly back to the hang."
    ),
    Exercise(
        164,
        "Hanging Windshield Wiper",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Hanging windshield wiper targeting the obliques.",
        "Hang from a bar and raise your legs until they point upward. Keep your legs together and core tight. Rotate your legs from side to side like windshield wipers. Move under control through the full range."
    ),
    Exercise(
        165,
        "Lying Leg Raise",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Lying leg raise targeting the lower abs.",
        "Lie flat on your back with your hands at your sides. Keep your legs straight and together. Raise your legs to ninety degrees while keeping your lower back down. Lower them slowly without touching the floor."
    ),
    Exercise(
        166,
        "V-Up",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "V-up for total ab development.",
        "Lie flat on your back with your arms extended overhead. Brace your core to begin. Simultaneously raise your legs and torso to reach toward your toes. Lower back down under control to the start."
    ),
    Exercise(
        167,
        "Plank",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Standard forearm plank for core stability.",
        "Rest on your forearms with elbows under your shoulders. Extend your legs back with toes on the floor. Keep your body in a straight line from head to heels. Brace your core and hold the position."
    ),
    Exercise(
        168,
        "Side Plank",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Side plank for oblique and lateral core stability.",
        "Lie on your side and prop yourself on one forearm. Stack your feet and lift your hips off the floor. Keep your body in a straight line from head to feet. Brace your core and hold, then switch sides."
    ),
    Exercise(
        169,
        "Plank (Weighted)",
        MuscleGroup.ABS,
        Equipment.PLATE,
        "Weighted forearm plank for an increased core challenge.",
        "Rest on your forearms with your body in a straight line. Have a partner place a weight plate on your upper back. Brace your core and keep your hips level. Hold the position for the desired time."
    ),
    Exercise(
        170,
        "High Plank",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "High plank on the hands for core stability.",
        "Place your hands under your shoulders with arms straight. Extend your legs back with toes on the floor. Keep your body in a straight line from head to heels. Brace your core and hold the position."
    ),
    Exercise(
        171,
        "Ab Wheel Rollout",
        MuscleGroup.ABS,
        Equipment.OTHER,
        "Kneeling ab wheel rollout for core strength.",
        "Kneel on the floor gripping the ab wheel beneath your shoulders. Brace your core and keep a slight tuck in your hips. Roll the wheel forward extending your body as far as you can control. Pull the wheel back using your abs to return.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        172,
        "Ab Wheel Rollout (Standing)",
        MuscleGroup.ABS,
        Equipment.OTHER,
        "Standing ab wheel rollout for advanced core strength.",
        "Stand and bend down to grip the ab wheel on the floor. Brace your core hard before moving. Roll the wheel forward into a near plank position. Pull the wheel back using your abs to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        173,
        "Russian Twist",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Rotational core exercise targeting the obliques.",
        "Sit on the floor with your knees bent and feet slightly off the ground. Lean back to engage your core and keep your spine long. Rotate your torso to one side and tap the floor beside your hip. Twist to the other side and repeat, alternating with control."
    ),
    Exercise(
        174,
        "Russian Twist (Weighted)",
        MuscleGroup.ABS,
        Equipment.MEDICINE_BALL,
        "Weighted rotational twist for the obliques using a medicine ball.",
        "Sit on the floor with your knees bent and feet lifted off the ground. Hold a medicine ball with both hands close to your chest. Lean back slightly and rotate your torso to tap the ball beside one hip. Twist to the other side and continue alternating under control."
    ),
    Exercise(
        175,
        "Mountain Climber",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Dynamic core and cardio movement performed from a plank.",
        "Start in a high plank with hands under your shoulders. Brace your core and keep your hips level. Drive one knee toward your chest, then quickly switch legs. Continue alternating at a rapid pace while keeping your back flat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.QUADS, MuscleGroup.CARDIO)
    ),
    Exercise(
        176,
        "Mountain Climber (Cross-Body)",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Cross-body mountain climber that emphasizes the obliques.",
        "Begin in a high plank with your hands under your shoulders. Keep your hips low and your core braced. Drive one knee toward the opposite elbow across your body. Return and repeat on the other side, alternating quickly.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.QUADS, MuscleGroup.CARDIO)
    ),
    Exercise(
        177,
        "Dead Bug",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Core stability drill that trains anti-extension control.",
        "Lie on your back with arms reaching toward the ceiling and knees bent over your hips. Press your lower back into the floor to brace your core. Slowly extend one arm overhead and the opposite leg toward the floor. Return to the start and repeat on the other side, alternating with control."
    ),
    Exercise(
        178,
        "Dead Bug (Weighted)",
        MuscleGroup.ABS,
        Equipment.MEDICINE_BALL,
        "Weighted dead bug for added core stability challenge.",
        "Lie on your back holding a medicine ball with arms extended over your chest. Bend your knees over your hips and press your lower back into the floor. Slowly lower one leg toward the floor while keeping the ball steady overhead. Return to the start and alternate legs under control."
    ),
    Exercise(
        179,
        "Hollow Body Hold",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Isometric hold that builds core endurance and tension.",
        "Lie flat on your back with your arms extended overhead. Press your lower back into the floor and brace your abs. Lift your shoulders and legs off the ground to form a shallow banana shape. Hold the position while breathing steadily."
    ),
    Exercise(
        180,
        "Toe Touch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Crunch variation targeting the upper abs.",
        "Lie on your back with your legs extended straight toward the ceiling. Reach both hands up toward your toes. Crunch your upper back off the floor by contracting your abs. Lower back down with control and repeat."
    ),
    Exercise(
        181,
        "Sit-Up",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Classic sit-up for overall abdominal development.",
        "Lie on your back with your knees bent and feet flat on the floor. Place your hands across your chest or behind your head. Curl your torso up until your chest moves toward your knees. Lower yourself back down with control and repeat."
    ),
    Exercise(
        182,
        "Sit-Up (Weighted)",
        MuscleGroup.ABS,
        Equipment.PLATE,
        "Weighted sit-up for progressive overload of the abs.",
        "Lie on your back with knees bent and feet flat on the floor. Hold a weight plate against your chest with both hands. Curl your torso up toward your knees while keeping the plate secure. Lower back down under control and repeat."
    ),
    Exercise(
        183,
        "Dragon Flag",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Advanced core exercise that trains full-body tension.",
        "Lie on a bench and grip its edge behind your head for support. Brace your core and lift your entire body so it points upward in a straight line. Keep your body rigid as you slowly lower it toward the bench. Stop just short of touching down and repeat.",
        secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        184,
        "L-Sit",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Isometric hold building core and hip flexor strength.",
        "Support your bodyweight on your hands or a pair of parallel bars. Press down through your hands and lift your hips. Extend your legs straight out in front of you to form an L shape. Hold the position while keeping your core tight.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS, MuscleGroup.QUADS)
    ),
    Exercise(
        185,
        "Knee Tuck Oblique Crunch",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Knee tuck with a side twist to engage the obliques.",
        "Sit or lie back with your torso leaning slightly away from your knees. Brace your core to support your spine. Pull your knees toward your chest while twisting your torso to one side. Lower and repeat, alternating the twist to each side."
    ),
    Exercise(
        186,
        "Captain Chair Leg Raise",
        MuscleGroup.ABS,
        Equipment.MACHINE,
        "Hanging knee raise targeting the lower abs.",
        "Rest your forearms on the pads and grip the handles of the captain chair. Let your legs hang straight down with your back against the pad. Raise your knees toward your chest by contracting your lower abs. Lower your legs slowly back to the start and repeat."
    ),
    Exercise(
        187,
        "Cable Pallof Press",
        MuscleGroup.ABS,
        Equipment.CABLE,
        "Anti-rotation core press using a cable.",
        "Stand sideways to a cable machine with the handle held at your chest. Set your feet shoulder width apart and brace your core. Press the handle straight out in front of you while resisting any rotation. Hold briefly, return to your chest, then repeat before switching sides."
    ),
    Exercise(
        188,
        "Cable Core Rotation",
        MuscleGroup.ABS,
        Equipment.CABLE,
        "Cable rotation movement for oblique strength.",
        "Stand sideways to a cable machine and grip the handle with both hands. Keep your arms extended and your core braced. Rotate your torso to pull the handle across your body. Return to the start under control and repeat before switching sides."
    ),
    Exercise(
        189,
        "Dumbbell Pullover On Stability Ball",
        MuscleGroup.ABS,
        Equipment.DUMBBELL,
        "Dumbbell pullover on a stability ball that works the chest, lats, and core.",
        "Sit on a stability ball and walk your feet forward until your upper back is supported and your hips are lifted into a bridge. Hold a single dumbbell with both hands directly above your chest with arms extended. Lower the dumbbell back behind your head in an arc while keeping a slight elbow bend. Pull it back over your chest while bracing your core and squeezing your lats.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.GLUTES)
    ),
    Exercise(
        190,
        "Stability Ball Crunch",
        MuscleGroup.ABS,
        Equipment.OTHER,
        "Crunch on a stability ball for an extended range of motion.",
        "Sit on a stability ball and walk your feet forward until your lower back rests on the ball. Place your hands behind your head or across your chest. Extend back over the ball, then crunch your upper body up by contracting your abs. Lower back over the ball with control and repeat."
    ),
    Exercise(
        191,
        "Side Bend (Dumbbell)",
        MuscleGroup.ABS,
        Equipment.DUMBBELL,
        "Standing side bend with a dumbbell for the obliques.",
        "Stand tall holding a dumbbell in one hand at your side. Keep your feet shoulder width apart and your core engaged. Bend laterally toward the weighted side, lowering the dumbbell along your leg. Return to upright by contracting your obliques and repeat before switching sides."
    ),
    Exercise(
        192,
        "Side Bend (Cable)",
        MuscleGroup.ABS,
        Equipment.CABLE,
        "Standing cable side bend for the obliques.",
        "Stand sideways to a low cable and grip the handle in the near hand. Set your feet shoulder width apart and brace your core. Bend laterally away from the machine against the cable resistance. Return to upright by contracting your obliques and repeat before switching sides."
    ),
    Exercise(
        193,
        "Ab Rollout (Barbell)",
        MuscleGroup.ABS,
        Equipment.BARBELL,
        "Kneeling rollout using a loaded barbell to challenge the core.",
        "Kneel on the floor and grip a loaded barbell with both hands. Brace your core and keep a flat back. Roll the bar forward, extending your body into a straight line. Pull the bar back toward your knees by contracting your abs.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        194,
        "Barbell Back Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Classic back squat for overall lower body strength.",
        "Set the bar across your upper back and step out of the rack. Place your feet shoulder width apart with toes slightly out. Squat down until your thighs are at least parallel to the floor. Drive up through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        195,
        "Barbell Front Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Front-loaded squat emphasizing the quads and upper back.",
        "Rest the bar across your front delts with elbows pointed forward. Step out and set your feet shoulder width apart. Squat down while keeping your torso upright and chest tall. Drive up through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        196,
        "Barbell Overhead Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Overhead squat that demands mobility and core stability.",
        "Hold the barbell locked out overhead with a wide grip. Set your feet shoulder width apart and brace your core. Squat down while keeping the bar balanced directly over your head. Drive up through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        197,
        "Dumbbell Goblet Squat",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Goblet squat holding a dumbbell at the chest.",
        "Hold a dumbbell vertically against your chest with both hands. Set your feet shoulder width apart with toes slightly out. Squat down while keeping your torso upright and elbows inside your knees. Drive up through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        198,
        "Dumbbell Split Squat",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Stationary split squat for unilateral quad development.",
        "Stand in a staggered stance holding a dumbbell in each hand. Keep your front foot flat and your torso upright. Lower your back knee toward the floor by bending both legs. Drive up through your front heel and repeat before switching legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        199,
        "Dumbbell Bulgarian Split Squat",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Single-leg split squat with the rear foot elevated.",
        "Hold a dumbbell in each hand and place your rear foot on a bench. Set your front foot far enough forward for a comfortable stance. Lower your back knee toward the floor while keeping your torso upright. Drive up through your front heel and repeat before switching legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        200,
        "Barbell Bulgarian Split Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Rear-foot-elevated split squat with a barbell on the back.",
        "Set the bar across your upper back and place your rear foot on a bench. Position your front foot forward for a stable stance. Lower your back knee toward the floor while keeping your chest up. Drive up through your front heel and repeat before switching legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        201,
        "Dumbbell Step-Up",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Step-up onto a box while holding dumbbells.",
        "Hold a dumbbell in each hand and stand facing a sturdy box. Place one foot flat on top of the box. Drive through that heel to step up until your leg is fully extended. Step back down with control and repeat, alternating legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        202,
        "Barbell Step-Up",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Step-up onto a box with a barbell on the back.",
        "Set the bar across your upper back and stand facing a sturdy box. Place one foot flat on top of the box. Drive through that heel to step up until your leg is fully extended. Step back down with control and repeat, alternating legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        203,
        "Leg Press",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Machine leg press for building lower body strength.",
        "Sit in the leg press with your feet shoulder width apart on the platform. Release the safeties and hold the handles. Lower the platform by bending your knees toward your chest. Press back up through your heels without locking your knees.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        204,
        "Leg Press (Single Leg)",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Single-leg press for balanced unilateral development.",
        "Sit in the leg press and place one foot centered on the platform. Release the safeties and hold the handles. Lower the platform by bending that knee toward your chest. Press back up through your heel and repeat before switching legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        205,
        "Leg Extension",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Machine leg extension that isolates the quads.",
        "Sit in the machine with your shins behind the padded roller. Adjust the pad so it rests just above your ankles. Extend your legs until they are straight, squeezing your quads at the top. Lower the weight slowly back to the start and repeat."
    ),
    Exercise(
        206,
        "Leg Extension (Single Leg)",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Single-leg extension for isolated unilateral quad work.",
        "Sit in the machine with one shin behind the padded roller. Adjust the pad so it sits just above your ankle. Extend that leg until it is straight, squeezing the quad at the top. Lower slowly back to the start and repeat before switching legs."
    ),
    Exercise(
        207,
        "Hack Squat",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Machine hack squat for quad-focused squatting.",
        "Position yourself in the hack squat machine with your shoulders under the pads. Place your feet shoulder width apart on the platform. Lower yourself by bending your knees until your thighs are parallel. Press back up through your heels to the start.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        208,
        "Smith Machine Squat",
        MuscleGroup.QUADS,
        Equipment.SMITH_MACHINE,
        "Guided squat on the Smith machine for added stability.",
        "Set the bar across your upper back and unrack it by rotating the hooks. Place your feet shoulder width apart and slightly in front of you. Squat down along the guided path until your thighs are parallel. Press up through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        209,
        "Dumbbell One Arm Lunge",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Forward lunge holding a single dumbbell in one hand.",
        "Stand tall holding a dumbbell in one hand at your side. Step forward with the opposite leg into a lunge. Lower your back knee toward the floor while keeping your torso upright. Push back through your front heel to the start and repeat before switching sides.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        210,
        "Weighted Stretch Lunge",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Deep lunge holding dumbbells to emphasize the hip flexor stretch.",
        "Stand holding a dumbbell in each hand at your sides. Step one foot far forward into a deep lunge. Sink your hips down and hold the bottom position briefly for a deep stretch. Push back through your front heel to the start and repeat on the other side.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        211,
        "Battling Ropes Alternate Arms Side Lunge",
        MuscleGroup.QUADS,
        Equipment.OTHER,
        "Lateral lunge combined with alternating battle rope waves.",
        "Hold a battle rope end in each hand with feet wider than shoulder width. Step out laterally into a side lunge on one leg. As you lunge, create alternating waves by whipping the ropes up and down. Return to center and repeat to the other side, keeping the ropes moving.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.SHOULDERS, MuscleGroup.CARDIO)
    ),
    Exercise(
        212,
        "Suspender Lunge Back Crossover",
        MuscleGroup.QUADS,
        Equipment.SUSPENSION,
        "Curtsy lunge variation using suspension straps for balance.",
        "Hold a suspension strap handle in each hand for support. Stand tall on one leg as your working leg. Step the other leg back and across behind your standing leg, lowering your hips. Drive back up to standing and repeat before switching sides.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        213,
        "Kettlebell Forward Lunge and Press",
        MuscleGroup.QUADS,
        Equipment.KETTLEBELL,
        "Compound movement combining a forward lunge with an overhead press.",
        "Hold a kettlebell at shoulder height in the racked position. Step forward into a lunge, lowering your back knee toward the floor. As you stand up out of the lunge, press the kettlebell overhead. Lower the weight back to your shoulder and repeat before switching sides.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    ),
    Exercise(
        214,
        "Dumbbell Lunge",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Stationary lunge for weighted single-leg leg work.",
        "Hold a dumbbell in each hand at your sides. Step forward or backward into a lunge with one leg. Lower your back knee toward the floor while keeping your torso upright. Push back through your front heel to the start and repeat, alternating legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        215,
        "Dumbbell Walking Lunge",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Walking lunge performed with dumbbells.",
        "Hold a dumbbell in each hand at your sides. Step forward into a lunge, lowering your back knee toward the floor. Drive through your front heel and bring your back leg forward into the next lunge. Continue walking forward, alternating legs with each step.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        216,
        "Sissy Squat",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Sissy squat for quad isolation.",
        "Stand tall and rise onto the balls of your feet. Lean your torso back while driving your knees forward. Lower your body until you feel a deep stretch in your quads. Push through your toes to return to the starting position."
    ),
    Exercise(
        217,
        "Pistol Squat",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Single-leg squat for unilateral leg strength.",
        "Stand on one leg with the other extended straight in front of you. Lower yourself slowly into a full squat on the working leg. Keep your chest up and arms forward for balance. Drive through your heel to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        218,
        "Wall Sit",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Isometric wall sit for quad endurance.",
        "Stand with your back flat against a wall. Slide down until your thighs are parallel to the ground. Keep your knees stacked over your ankles. Hold the position for the prescribed time."
    ),
    Exercise(
        219,
        "Goblet Squat (Kettlebell)",
        MuscleGroup.QUADS,
        Equipment.KETTLEBELL,
        "Squat holding a kettlebell at the chest.",
        "Hold a kettlebell close to your chest with both hands. Set your feet shoulder width apart. Squat down keeping your torso upright and elbows inside your knees. Drive through your heels to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        220,
        "Belt Squat",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "Squat that loads the hips without loading the spine.",
        "Attach the weighted belt around your hips and stand on the platform. Brace your core and grip the handles for balance. Lower into a squat until your thighs are parallel. Push through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        221,
        "Landmine Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Squat holding the end of a landmine bar at the chest.",
        "Hold the end of the landmine bar against your chest with both hands. Stand with feet shoulder width apart. Squat down keeping your torso upright. Drive through your heels to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        222,
        "Trap Bar Squat",
        MuscleGroup.QUADS,
        Equipment.TRAP_BAR,
        "Squat performed inside a trap bar for an upright torso.",
        "Step inside the trap bar and grip the handles at your sides. Set your feet shoulder width apart and brace your core. Squat down then drive up through your heels. Keep your chest tall throughout the lift.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        223,
        "Shrimp Squat",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Advanced single-leg squat with the rear foot held.",
        "Stand on one leg and hold the opposite foot behind you. Lower yourself down until your back knee nearly touches the floor. Keep your chest up and balance throughout. Drive through your standing heel to return to the top.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        224,
        "Box Jump",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Explosive jump onto a raised box.",
        "Stand facing a sturdy box with feet shoulder width apart. Dip into a quarter squat and swing your arms back. Explode upward and land softly on top of the box with bent knees. Step back down and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.CALVES)
    ),
    Exercise(
        225,
        "Jump Squat",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Explosive squat with a jump at the top.",
        "Stand with feet shoulder width apart. Lower into a squat keeping your chest up. Explode upward and jump off the ground. Land softly with bent knees and immediately sink into the next rep.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.CALVES)
    ),
    Exercise(
        226,
        "Split Jump",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Explosive lunge that switches legs in mid-air.",
        "Start in a split stance with one foot forward and one back. Lower into a lunge until both knees are bent. Explode upward and switch your leg positions in the air. Land softly into the opposite lunge and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        227,
        "Romanian Deadlift (Barbell)",
        MuscleGroup.HAMSTRINGS,
        Equipment.BARBELL,
        "Hip-hinge deadlift variation for hamstring development.",
        "Hold the barbell at your hips with an overhand grip. Keep a slight bend in your knees and brace your core. Hinge at the hips pushing your glutes back while lowering the bar along your legs. Feel a stretch in your hamstrings then drive your hips forward to return.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        228,
        "Romanian Deadlift (Dumbbell)",
        MuscleGroup.HAMSTRINGS,
        Equipment.DUMBBELL,
        "Dumbbell hip-hinge variation for the hamstrings.",
        "Hold a dumbbell in each hand in front of your thighs. Keep a slight bend in your knees and your back flat. Hinge at the hips lowering the weights close to your legs. Drive your hips forward to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        229,
        "Stiff-Leg Deadlift (Barbell)",
        MuscleGroup.HAMSTRINGS,
        Equipment.BARBELL,
        "Deadlift with nearly straight legs for hamstring emphasis.",
        "Hold the barbell with an overhand grip and keep your legs nearly straight. Brace your core and maintain a flat back. Hinge at the hips lowering the bar toward the floor. Squeeze your hamstrings and glutes to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        230,
        "Single-Leg Romanian Deadlift",
        MuscleGroup.HAMSTRINGS,
        Equipment.DUMBBELL,
        "Single-leg hip hinge for unilateral hamstring work.",
        "Stand on one leg holding a dumbbell in the opposite hand. Hinge forward at the hips while extending your free leg behind you. Lower the dumbbell toward the floor keeping your back flat. Drive through your standing heel to return upright.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        231,
        "Lying Leg Curl",
        MuscleGroup.HAMSTRINGS,
        Equipment.MACHINE,
        "Lying machine curl for hamstring isolation.",
        "Lie face down on the machine with the pad against your lower calves. Grip the handles and keep your hips pressed into the bench. Curl your legs up squeezing your hamstrings. Lower the weight back down under control."
    ),
    Exercise(
        232,
        "Lying Leg Curl (Single Leg)",
        MuscleGroup.HAMSTRINGS,
        Equipment.MACHINE,
        "Single-leg lying curl for unilateral hamstring work.",
        "Lie face down on the machine with one ankle behind the pad. Keep your hips pressed down into the bench. Curl one leg up squeezing the hamstring. Lower under control and repeat before switching legs."
    ),
    Exercise(
        233,
        "Seated Leg Curl (Single Leg)",
        MuscleGroup.HAMSTRINGS,
        Equipment.MACHINE,
        "Single-leg seated curl for hamstring development.",
        "Sit in the machine with one leg over the pad and the thigh restraint locked. Keep your back against the seat. Curl your leg down squeezing the hamstring. Return slowly to the start and repeat before switching legs."
    ),
    Exercise(
        234,
        "Seated Leg Curl",
        MuscleGroup.HAMSTRINGS,
        Equipment.MACHINE,
        "Seated machine curl for hamstring development.",
        "Sit in the machine with both legs over the pad and the thigh restraint locked. Keep your back against the seat. Curl your legs down squeezing your hamstrings. Return slowly under control to the starting position."
    ),
    Exercise(
        235,
        "Nordic Hamstring Curl",
        MuscleGroup.HAMSTRINGS,
        Equipment.BODYWEIGHT,
        "Bodyweight eccentric curl for hamstring strength.",
        "Kneel upright with your ankles secured under a pad or held by a partner. Keep your hips extended and your body in a straight line. Lower your torso forward as slowly as you can control. Catch yourself with your hands and push back to the start."
    ),
    Exercise(
        236,
        "Glute Ham Raise",
        MuscleGroup.HAMSTRINGS,
        Equipment.MACHINE,
        "Posterior chain raise on a glute-ham developer.",
        "Position yourself in the machine with your feet anchored and knees on the pad. Lower your torso forward keeping your body straight. Use your hamstrings and glutes to pull yourself back up. Control the movement throughout the range.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        237,
        "Good Morning",
        MuscleGroup.HAMSTRINGS,
        Equipment.BARBELL,
        "Barbell hip hinge for the hamstrings and lower back.",
        "Rest the barbell across your upper back and set your feet shoulder width apart. Keep a slight bend in your knees and brace your core. Hinge at the hips lowering your torso toward parallel. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        238,
        "Twisting Hyperextension 45 Degree",
        MuscleGroup.LOWER_BACK,
        Equipment.MACHINE,
        "Hyperextension on a 45-degree bench with a torso twist.",
        "Position your hips on the pad with your ankles secured. Lower your torso toward the floor under control. Extend back up by contracting your lower back. Rotate your upper body to one side at the top of the movement.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        239,
        "Cable Leg Curl",
        MuscleGroup.HAMSTRINGS,
        Equipment.CABLE,
        "Cable curl for hamstring isolation.",
        "Attach an ankle strap to a low cable and secure it to your ankle. Face the machine and hold on for balance. Curl your leg back squeezing your hamstring. Return slowly under control and repeat."
    ),
    Exercise(
        240,
        "Cable Pull-Through",
        MuscleGroup.HAMSTRINGS,
        Equipment.CABLE,
        "Cable hip hinge for the posterior chain.",
        "Face away from a low cable with the rope between your legs. Hinge back at the hips letting the cable pull your hands between your legs. Keep your back flat and knees slightly bent. Thrust your hips forward to stand tall and squeeze your glutes.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        241,
        "Barbell Hip Thrust",
        MuscleGroup.GLUTES,
        Equipment.BARBELL,
        "Bench-supported barbell thrust for glute development.",
        "Sit with your upper back against a bench and roll the barbell over your hips. Plant your feet flat and shoulder width apart. Drive through your heels thrusting your hips up until your torso is parallel to the floor. Squeeze your glutes at the top then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        242,
        "Barbell Glute Bridge",
        MuscleGroup.GLUTES,
        Equipment.BARBELL,
        "Floor-based barbell bridge for the glutes.",
        "Lie on your back with the barbell resting across your hips. Plant your feet flat and close to your glutes. Drive through your heels thrusting your hips toward the ceiling. Squeeze your glutes at the top then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        243,
        "Dumbbell Hip Thrust",
        MuscleGroup.GLUTES,
        Equipment.DUMBBELL,
        "Bench-supported dumbbell thrust for the glutes.",
        "Sit with your upper back against a bench and hold a dumbbell on your hips. Plant your feet flat and shoulder width apart. Drive through your heels thrusting your hips up. Squeeze your glutes at the top then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        244,
        "Single-Leg Hip Thrust",
        MuscleGroup.GLUTES,
        Equipment.BODYWEIGHT,
        "Bench-supported single-leg thrust for unilateral glute work.",
        "Rest your upper back on a bench with one foot planted on the floor. Extend the other leg out straight. Drive through your planted heel thrusting your hips up. Squeeze your glute at the top then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        245,
        "Smith Machine Shrugs",
        MuscleGroup.TRAPS,
        Equipment.SMITH_MACHINE,
        "Smith machine shrug for trapezius development.",
        "Stand inside the Smith machine and grip the bar at hip height. Unrack the bar and let it hang with arms straight. Shrug your shoulders straight up toward your ears. Lower slowly back down under control."
    ),
    Exercise(
        246,
        "Cable Kickback",
        MuscleGroup.GLUTES,
        Equipment.CABLE,
        "Cable kickback for glute isolation.",
        "Attach an ankle strap to a low cable and secure it to your ankle. Face the machine and hold on for support. Extend your leg straight back squeezing your glute. Return slowly under control and repeat."
    ),
    Exercise(
        247,
        "Hip Abduction Machine",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Seated machine abduction for the glute medius.",
        "Sit in the machine with the outer pads against your thighs. Keep your back against the seat. Push your legs outward against the resistance. Return slowly to the starting position under control."
    ),
    Exercise(
        248,
        "Hip Adduction Machine",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Seated machine adduction for the inner thighs.",
        "Sit in the machine with the pads against the inside of your thighs. Keep your back against the seat. Squeeze your legs together against the resistance. Return slowly to the open position under control."
    ),
    Exercise(
        249,
        "Resistance Band Clamshell",
        MuscleGroup.GLUTES,
        Equipment.RESISTANCE_BAND,
        "Banded clamshell for glute activation.",
        "Lie on your side with a band looped above your knees and your knees bent. Stack your hips and keep your feet together. Open your top knee toward the ceiling like a clamshell. Lower it back down under control and repeat."
    ),
    Exercise(
        250,
        "Resistance Band Lateral Walk",
        MuscleGroup.GLUTES,
        Equipment.RESISTANCE_BAND,
        "Banded lateral walk for glute activation.",
        "Place a band above your knees and sink into a quarter squat. Keep tension on the band throughout. Step sideways with one foot then follow with the other. Continue stepping in one direction then reverse."
    ),
    Exercise(
        251,
        "Curtsy Lunge",
        MuscleGroup.GLUTES,
        Equipment.BODYWEIGHT,
        "Crossover lunge targeting the glutes.",
        "Stand tall with your feet hip width apart. Step one leg behind and across your body. Lower into a lunge until both knees are bent. Drive through your front heel to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        252,
        "Frog Pump",
        MuscleGroup.GLUTES,
        Equipment.BODYWEIGHT,
        "Frog pump for glute activation.",
        "Lie on your back with the soles of your feet together and knees out wide. Let your heels rest close to your body. Thrust your hips up squeezing your glutes at the top. Lower back down under control and repeat."
    ),
    Exercise(
        253,
        "Reverse Hyper Machine",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Reverse hyper for the glutes and lower back.",
        "Lie face down on the machine with your hips at the edge of the pad. Grip the handles and let your legs hang down. Raise your legs behind you until they are level with your torso. Lower under control squeezing your glutes throughout.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        254,
        "Sumo Squat",
        MuscleGroup.GLUTES,
        Equipment.DUMBBELL,
        "Wide-stance dumbbell squat for the glutes.",
        "Stand with a wide stance and toes pointed outward. Hold a dumbbell vertically between your legs. Squat down keeping your chest up and knees tracking over your toes. Drive through your heels to stand and squeeze your glutes.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        255,
        "Machine Hip Thrust",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Machine-based hip thrust for the glutes.",
        "Sit in the hip thrust machine and position the pad across your hips. Plant your feet firmly on the platform. Thrust your hips up against the resistance. Squeeze your glutes at the top then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        256,
        "Dumbbell Reverse Lunge",
        MuscleGroup.GLUTES,
        Equipment.DUMBBELL,
        "Reverse lunge loaded with dumbbells.",
        "Hold a dumbbell in each hand at your sides. Step one foot back and lower into a lunge until both knees are bent. Keep your front shin vertical and torso upright. Drive through your front heel to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        257,
        "Machine Glute Kickback",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Machine kickback for glute isolation.",
        "Position yourself in the machine with one foot against the lever pad. Grip the handles and keep your torso supported. Extend your leg back squeezing your glute. Return slowly under control and repeat before switching legs."
    ),
    Exercise(
        258,
        "Standing Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Standing machine calf raise for the gastrocnemius.",
        "Stand on the platform with your shoulders under the pads and the balls of your feet on the edge. Let your heels drop for a full stretch. Raise up onto your toes as high as possible. Lower slowly back down under control."
    ),
    Exercise(
        259,
        "Standing Calf Raise (Barbell)",
        MuscleGroup.CALVES,
        Equipment.BARBELL,
        "Standing calf raise with a barbell across the upper back.",
        "Position a barbell across your upper back and stand with the balls of your feet on a raised block. Keep your legs straight and your torso upright. Raise up onto your toes as high as possible. Lower your heels below the block for a full stretch and repeat."
    ),
    Exercise(
        260,
        "Standing Calf Raise (Dumbbell)",
        MuscleGroup.CALVES,
        Equipment.DUMBBELL,
        "Standing calf raise holding dumbbells at the sides.",
        "Hold a dumbbell in each hand with your arms hanging at your sides. Stand with the balls of your feet on a raised block. Press up onto your toes as high as you can. Lower your heels under control for a full stretch and repeat."
    ),
    Exercise(
        261,
        "Standing Calf Raise (Single Leg)",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Single-leg standing calf raise for one calf at a time.",
        "Stand on the platform with the ball of one foot and tuck the other leg behind you. Hold a support for balance. Raise up onto your toes and squeeze the calf at the top. Lower your heel slowly for a full stretch and repeat before switching legs."
    ),
    Exercise(
        262,
        "Seated Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Seated calf raise that emphasizes the soleus.",
        "Sit on the machine and place the balls of your feet on the platform. Position the pad snugly over your knees. Press up onto your toes and squeeze your calves at the top. Lower your heels for a deep stretch and repeat."
    ),
    Exercise(
        263,
        "Donkey Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Donkey calf raise that stretches the gastrocnemius.",
        "Place the balls of your feet on the platform and bend forward at the hips. Position the weight pad across your lower back. Raise up onto your toes as high as possible. Lower your heels below the platform for a full stretch and repeat."
    ),
    Exercise(
        264,
        "Leg Press Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Calf raise performed on the leg press machine.",
        "Sit in the leg press and place the balls of your feet on the bottom edge of the platform. Keep your legs nearly straight. Press the platform away by extending through your toes. Lower your heels back for a full stretch and repeat."
    ),
    Exercise(
        265,
        "Bodyweight Calf Raise",
        MuscleGroup.CALVES,
        Equipment.BODYWEIGHT,
        "Bodyweight standing calf raise on a step.",
        "Stand with the balls of your feet on the edge of a step. Hold a wall or rail for balance. Raise up onto your toes as high as possible. Lower your heels below the step for a full stretch and repeat."
    ),
    Exercise(
        266,
        "Bodyweight Calf Raise (Single Leg)",
        MuscleGroup.CALVES,
        Equipment.BODYWEIGHT,
        "Single-leg bodyweight calf raise on a step.",
        "Stand on one foot with the ball of that foot on the edge of a step. Hold a support for balance. Raise up onto your toes and squeeze the calf. Lower your heel below the step for a full stretch and repeat before switching legs."
    ),
    Exercise(
        267,
        "Jump Rope (Calves)",
        MuscleGroup.CALVES,
        Equipment.BODYWEIGHT,
        "Jump rope drill for calf endurance and conditioning.",
        "Hold the rope handles and stand tall with a slight bend in your knees. Bounce lightly on the balls of your feet as you turn the rope. Keep your jumps low and continuous to engage the calves. Maintain a steady rhythm for the desired duration."
    ),
    Exercise(
        268,
        "Trap Bar Jump Squat",
        MuscleGroup.QUADS,
        Equipment.TRAP_BAR,
        "Explosive jump squat using a trap bar to build lower body power.",
        "Stand inside the trap bar and grip the handles with an upright torso. Descend into a quarter squat while keeping your chest up. Drive through your legs and explosively jump upward. Land softly and absorb the impact before the next rep.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES)
    ),
    Exercise(
        269,
        "Barbell Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.BARBELL,
        "Wrist curl targeting the forearm flexors.",
        "Sit and rest your forearms on a bench with your palms facing up. Let the barbell roll toward your fingertips. Curl the bar upward using only your wrists. Lower it under control and repeat."
    ),
    Exercise(
        270,
        "Barbell Reverse Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.BARBELL,
        "Reverse wrist curl targeting the forearm extensors.",
        "Sit and rest your forearms on a bench with your palms facing down. Let your wrists drop toward the floor. Curl the bar upward using only your wrists. Lower it slowly and repeat."
    ),
    Exercise(
        271,
        "Standing Hammer Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Neutral-grip curl emphasizing the brachioradialis.",
        "Hold a dumbbell in each hand with your palms facing each other. Keep your elbows tucked against your sides. Curl the dumbbells upward while maintaining the neutral grip. Lower them under control and repeat."
    ),
    Exercise(
        272,
        "Reverse Barbell Curl",
        MuscleGroup.FOREARMS,
        Equipment.BARBELL,
        "Overhand-grip barbell curl that works the forearms.",
        "Grip the barbell with your palms facing down at shoulder width. Keep your elbows close to your sides. Curl the bar up toward your shoulders. Lower it slowly back to the start and repeat."
    ),
    Exercise(
        273,
        "Farmer Walk",
        MuscleGroup.FOREARMS,
        Equipment.DUMBBELL,
        "Loaded carry that builds grip and forearm strength.",
        "Pick up a heavy dumbbell in each hand and stand tall. Brace your core and keep your shoulders back. Walk forward with controlled steps while maintaining a strong grip. Continue for the desired distance and set the weights down safely.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS, MuscleGroup.ABS)
    ),
    Exercise(
        274,
        "Plate Pinch",
        MuscleGroup.FOREARMS,
        Equipment.PLATE,
        "Pinch grip hold that strengthens the fingers and forearms.",
        "Pinch one or more weight plates together between your fingers and thumb. Stand tall and let your arm hang at your side. Hold the plates with a firm grip without letting them slip. Maintain the hold or walk for the desired time."
    ),
    Exercise(
        275,
        "Hand Gripper",
        MuscleGroup.FOREARMS,
        Equipment.OTHER,
        "Spring gripper drill for crushing grip strength.",
        "Hold the hand gripper in your palm with the handles spanning your fingers. Squeeze the handles fully together. Hold briefly at full closure. Release slowly under control and repeat for reps."
    ),
    Exercise(
        276,
        "Wrist Roller",
        MuscleGroup.FOREARMS,
        Equipment.OTHER,
        "Wrist roller drill for forearm endurance.",
        "Hold the wrist roller in front of you with a weight attached to the rope. Extend your arms out at shoulder height. Roll the weight up by alternating wrist rotations. Lower it back down under control and repeat."
    ),
    Exercise(
        277,
        "Dead Hang",
        MuscleGroup.FOREARMS,
        Equipment.BODYWEIGHT,
        "Passive hang from a bar for grip endurance.",
        "Reach up and grip a pull-up bar with both hands. Hang with your arms fully extended and feet off the ground. Keep your shoulders engaged and relax your lower body. Hold for as long as possible and release safely."
    ),
    Exercise(
        278,
        "Reverse Hyperextension",
        MuscleGroup.LOWER_BACK,
        Equipment.MACHINE,
        "Reverse hyperextension targeting the lower back and glutes.",
        "Lie face down on the pad with your hips at the edge and legs hanging down. Grip the handles for support. Raise your legs behind you until they are level with your torso. Squeeze your glutes and lower back, then lower under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        279,
        "Hyperextension",
        MuscleGroup.LOWER_BACK,
        Equipment.MACHINE,
        "Back extension targeting the lower back and glutes.",
        "Position your hips on the pad with your feet anchored under the supports. Cross your arms or place hands behind your head. Lower your torso by hinging at the hips. Raise back up to a neutral position while squeezing your glutes and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        280,
        "Hyperextension Weighted",
        MuscleGroup.LOWER_BACK,
        Equipment.MACHINE,
        "Weighted back extension for the lower back and glutes.",
        "Position your hips on the pad and hold a weight plate against your chest. Anchor your feet under the supports. Lower your torso by hinging at the hips. Raise back to neutral while squeezing your glutes and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        281,
        "Seated Triceps pushdown",
        MuscleGroup.TRICEPS,
        Equipment.MACHINE,
        "A seated machine exercise that targets the triceps using a vertical downward pushing motion.",
        "Adjust the seat height so the handles rest near chest level. Sit upright on the bench without leaning back and grip the handles. Keeping your elbows tucked close to your sides, press the handles straight down until your arms are fully extended. Squeeze your triceps at the bottom, then slowly raise your hands back up under control.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        282,
        "Good Morning",
        MuscleGroup.LOWER_BACK,
        Equipment.BARBELL,
        "Hip-hinge movement for the hamstrings and lower back.",
        "Rest a barbell across your upper back and stand with feet hip-width apart. Keep a slight bend in your knees and your back flat. Hinge forward at the hips until your torso is near parallel to the floor. Drive your hips forward to return to standing and repeat.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES)
    ),
    Exercise(
        283,
        "Superman",
        MuscleGroup.LOWER_BACK,
        Equipment.BODYWEIGHT,
        "Prone hold that builds lower back endurance.",
        "Lie face down on the floor with your arms extended overhead. Simultaneously raise your arms, chest, and legs off the ground. Squeeze your lower back and glutes at the top. Hold briefly, then lower under control and repeat."
    ),
    Exercise(
        284,
        "Bird Dog",
        MuscleGroup.LOWER_BACK,
        Equipment.BODYWEIGHT,
        "Core and lower back stability drill on all fours.",
        "Start on all fours with your hands under your shoulders and knees under your hips. Extend one arm forward and the opposite leg back simultaneously. Keep your hips level and your core braced. Hold briefly, return to the start, and switch sides.",
        secondaryMuscles = listOf(MuscleGroup.ABS, MuscleGroup.GLUTES)
    ),
    Exercise(
        285,
        "Barbell Rack Pull",
        MuscleGroup.LOWER_BACK,
        Equipment.BARBELL,
        "Partial deadlift from pins for the lower and upper back.",
        "Set the safety pins just below knee height and load the barbell. Grip the bar with your hands outside your knees and brace your back. Drive through your legs and extend your hips to lift the bar. Lower it back to the pins under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.TRAPS, MuscleGroup.GLUTES)
    ),
    Exercise(
        286,
        "Prone Cobra",
        MuscleGroup.LOWER_BACK,
        Equipment.BODYWEIGHT,
        "Prone hold that strengthens the lower back and posture.",
        "Lie face down with your arms at your sides and palms facing down. Raise your chest off the ground while squeezing your lower back. Rotate your thumbs outward and pull your shoulder blades together. Hold the position, then lower under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        287,
        "Barbell Shrugs",
        MuscleGroup.TRAPS,
        Equipment.BARBELL,
        "Barbell shrug for trapezius development.",
        "Hold a barbell in front of your thighs with an overhand grip. Stand tall with your arms straight. Shrug your shoulders straight up toward your ears. Squeeze at the top, then lower slowly and repeat."
    ),
    Exercise(
        288,
        "Dumbbell Shrugs",
        MuscleGroup.TRAPS,
        Equipment.DUMBBELL,
        "Dumbbell shrug for trap isolation.",
        "Hold a dumbbell in each hand at your sides with your arms straight. Stand tall with your chest up. Shrug your shoulders straight up toward your ears. Hold briefly at the top, then lower under control and repeat."
    ),
    Exercise(
        289,
        "Barbell High Pull",
        MuscleGroup.TRAPS,
        Equipment.BARBELL,
        "Explosive pull for the traps and upper back.",
        "Hold a barbell at arms length in front of your thighs. Dip slightly and explosively extend your hips. Pull the bar upward to chest height while leading with your elbows. Lower it under control and reset for the next rep.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)
    ),
    Exercise(
        290,
        "Barbell Upright Row",
        MuscleGroup.TRAPS,
        Equipment.BARBELL,
        "Upright row for the traps and lateral deltoids.",
        "Hold a barbell in front of your thighs with a shoulder-width grip. Keep the bar close to your body as you pull. Raise it to chest height while leading with your elbows. Lower it slowly back to the start and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BICEPS)
    ),
    Exercise(
        291,
        "Dumbbell Upright Row",
        MuscleGroup.TRAPS,
        Equipment.DUMBBELL,
        "Dumbbell upright row for the traps and deltoids.",
        "Hold a dumbbell in each hand in front of your thighs. Keep the dumbbells close to your body. Pull them upward alongside your torso while leading with your elbows. Lower them under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BICEPS)
    ),
    Exercise(
        292,
        "Cable Upright Row",
        MuscleGroup.TRAPS,
        Equipment.CABLE,
        "Cable upright row providing constant tension.",
        "Attach a straight bar to a low pulley and grip it at shoulder width. Stand tall with your arms extended down. Pull the bar upward toward your chest while leading with your elbows. Lower it slowly to maintain tension and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BICEPS)
    ),
    Exercise(
        293,
        "Face Pull",
        MuscleGroup.TRAPS,
        Equipment.CABLE,
        "Cable face pull for the rear delts and upper traps.",
        "Attach a rope to a high pulley and grip both ends with palms facing in. Step back and hold your arms extended. Pull the rope toward your face while externally rotating your hands. Squeeze your shoulder blades together, then return under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)
    ),
    Exercise(
        294,
        "Trap Bar Shrugs",
        MuscleGroup.TRAPS,
        Equipment.TRAP_BAR,
        "Shrug performed with a trap bar for the trapezius.",
        "Stand inside the trap bar and grip the handles at your sides. Stand tall with your arms straight. Shrug your shoulders straight up toward your ears. Squeeze at the top, then lower slowly and repeat."
    ),
    Exercise(
        295,
        "Burpee",
        MuscleGroup.FULL_BODY,
        Equipment.BODYWEIGHT,
        "Full-body conditioning movement combining a squat, plank, and jump.",
        "Begin standing, then squat down and place your hands on the floor. Kick your feet back into a push-up position. Jump your feet back toward your hands. Explode upward into a jump and land softly before the next rep.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.QUADS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        296,
        "Thruster",
        MuscleGroup.FULL_BODY,
        Equipment.BARBELL,
        "Barbell thruster combining a front squat and overhead press.",
        "Hold the barbell across your front shoulders with elbows up. Descend into a full front squat. Drive explosively out of the squat through your legs. Use that momentum to press the bar overhead, then lower it back to your shoulders and repeat.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    ),
    Exercise(
        297,
        "Clean and Press",
        MuscleGroup.FULL_BODY,
        Equipment.BARBELL,
        "Full-body lift driving the barbell from the floor to overhead.",
        "Grip the barbell on the floor with hands just outside your knees. Explosively extend your hips to clean the bar to your shoulders. Press the bar overhead to full lockout. Lower it back to the floor under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.QUADS, MuscleGroup.TRICEPS)
    ),
    Exercise(
        298,
        "Clean and Jerk",
        MuscleGroup.FULL_BODY,
        Equipment.BARBELL,
        "Olympic lift combining a clean with an explosive overhead jerk.",
        "Pull the barbell from the floor and catch it on your shoulders in a clean. Stand fully and dip slightly at the knees. Drive the bar upward while splitting your legs to receive it overhead. Recover to a standing position, then lower the bar and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.QUADS, MuscleGroup.TRICEPS)
    ),
    Exercise(
        299,
        "Snatch",
        MuscleGroup.FULL_BODY,
        Equipment.BARBELL,
        "Olympic lift moving the barbell from floor to overhead in one motion.",
        "Grip the barbell wide and set your back flat over the bar. Explosively extend your hips and pull the bar upward. Drop under the bar and catch it overhead in a squat. Stand up to full lockout, then lower the bar and repeat.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.QUADS, MuscleGroup.BACK)
    ),
    Exercise(
        300,
        "Power Clean",
        MuscleGroup.FULL_BODY,
        Equipment.BARBELL,
        "Explosive lift pulling the barbell from the floor to the shoulders.",
        "Set up over the barbell with hands just outside your knees and back flat. Explosively extend your hips and shrug to drive the bar upward. Pull yourself under and catch the bar on your shoulders in a quarter squat. Stand tall, then lower the bar and repeat.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.TRAPS, MuscleGroup.GLUTES)
    ),
    Exercise(
        301,
        "Kettlebell Swing",
        MuscleGroup.FULL_BODY,
        Equipment.KETTLEBELL,
        "Ballistic hip-hinge swing for power and conditioning.",
        "Stand with feet shoulder-width apart and grip the kettlebell with both hands. Hinge at the hips and swing the bell back between your legs. Explosively drive your hips forward to propel the bell to chest height. Let it swing back down and repeat in a fluid rhythm.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        302,
        "Kettlebell Turkish Get-Up",
        MuscleGroup.FULL_BODY,
        Equipment.KETTLEBELL,
        "Turkish get-up for full-body stability and control.",
        "Lie on your back and press the kettlebell straight up with one arm. Roll up onto your elbow and then your hand. Sweep your leg back and rise into a lunge. Stand up fully keeping the bell locked out overhead. Reverse each step to return to the floor.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ABS, MuscleGroup.GLUTES)
    ),
    Exercise(
        303,
        "Knee Tuck Jump",
        MuscleGroup.FULL_BODY,
        Equipment.BODYWEIGHT,
        "Explosive jump driving the knees high toward the chest.",
        "Start in a quarter squat with your arms ready. Jump vertically with maximum force. Pull both knees up toward your chest at the top. Land softly with bent knees and reset for the next rep.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.CALVES, MuscleGroup.ABS)
    ),
    Exercise(
        304,
        "Devil Press",
        MuscleGroup.FULL_BODY,
        Equipment.DUMBBELL,
        "Burpee combined with a dumbbell snatch for explosive power.",
        "Place two dumbbells on the floor and drop into a burpee. Lower your chest to the ground between the dumbbells. Drive up and swing the dumbbells from the floor to overhead in one motion. Lock the weights out above your head and return to the start.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES)
    ),
    Exercise(
        305,
        "Dumbbell Snatch",
        MuscleGroup.FULL_BODY,
        Equipment.DUMBBELL,
        "Single-arm dumbbell snatch from floor to overhead for power.",
        "Stand with the dumbbell on the floor between your feet. Hinge down and grip it with one hand. Explosively extend your hips and pull the dumbbell upward. Punch your hand up to lock it out overhead in one motion.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.TRAPS)
    ),
    Exercise(
        306,
        "Battle Ropes",
        MuscleGroup.FULL_BODY,
        Equipment.OTHER,
        "Battle ropes for full-body conditioning and endurance.",
        "Hold one rope end in each hand with a slight squat. Brace your core and keep your back flat. Drive your arms up and down to create alternating waves. Maintain a fast steady rhythm for the full interval.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ABS, MuscleGroup.FOREARMS)
    ),
    Exercise(
        307,
        "Medicine Ball Slam",
        MuscleGroup.FULL_BODY,
        Equipment.MEDICINE_BALL,
        "Explosive overhead slam driven by the core.",
        "Stand holding the medicine ball with both hands. Raise it fully overhead while extending your body. Slam it down to the floor with maximum force using your core. Catch or pick up the ball and repeat.",
        secondaryMuscles = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS, MuscleGroup.BACK)
    ),
    Exercise(
        308,
        "Dumbbell Thruster",
        MuscleGroup.FULL_BODY,
        Equipment.DUMBBELL,
        "Front squat into an overhead press in one fluid movement.",
        "Hold a dumbbell at each shoulder and stand tall. Squat down until your thighs are parallel to the floor. Drive up through your heels explosively. Use the momentum to press both dumbbells overhead. Lower them back to your shoulders and repeat.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.SHOULDERS, MuscleGroup.GLUTES, MuscleGroup.TRICEPS)
    ),
    Exercise(
        309,
        "Running",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Steady-pace running for cardiovascular endurance.",
        "Begin with a light warm-up to raise your heart rate. Run at a steady sustainable pace. Keep your breathing consistent and relaxed. Maintain upright posture and a smooth stride throughout."
    ),
    Exercise(
        310,
        "Sprinting",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Maximal-effort sprints for anaerobic power.",
        "Warm up thoroughly before sprinting. Accelerate to maximum effort over a short distance. Pump your arms and drive your knees hard. Rest fully between sprints and then repeat."
    ),
    Exercise(
        311,
        "Cycling",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Steady-pace cycling for cardiovascular endurance.",
        "Adjust the seat so your legs extend nearly fully at the bottom. Begin pedaling at an easy warm-up pace. Settle into a steady consistent cadence. Maintain smooth even pedal strokes throughout the ride."
    ),
    Exercise(
        312,
        "Rowing",
        MuscleGroup.CARDIO,
        Equipment.MACHINE,
        "Rowing machine for full-body cardio conditioning.",
        "Strap your feet in and grip the handle with arms extended. Drive back powerfully through your legs first. Lean back slightly and pull the handle to your chest. Return by extending your arms then bending your knees with control.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.QUADS, MuscleGroup.BICEPS)
    ),
    Exercise(
        313,
        "Jump Rope",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Jump rope for cardio and coordination.",
        "Hold the rope handles with the rope behind you. Swing the rope overhead using your wrists. Bounce lightly on the balls of your feet as it passes. Keep a steady consistent rhythm throughout."
    ),
    Exercise(
        314,
        "Assault Bike",
        MuscleGroup.CARDIO,
        Equipment.MACHINE,
        "Air bike for high-intensity full-body cardio.",
        "Sit on the bike and grip the moving handles. Push and pull the handles while pedaling. Drive at maximum effort for high-intensity intervals. Recover at an easy pace between bursts.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        315,
        "Recumbent Exercise Bike",
        MuscleGroup.CARDIO,
        Equipment.MACHINE,
        "Low-impact cycling performed in a reclined position.",
        "Sit back in the recumbent seat with your back supported. Place your feet on the pedals and set a comfortable resistance. Pedal smoothly at a steady cadence. Maintain consistent effort for the duration of your session."
    ),
    Exercise(
        316,
        "Stair Climbing",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Stair climbing for cardio and leg endurance.",
        "Begin at the base of the stairs with an upright posture. Climb at a steady controlled pace. Drive through each step with your full foot. Keep a consistent rhythm and breathing throughout.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.CALVES)
    ),
    Exercise(
        317,
        "Elliptical",
        MuscleGroup.CARDIO,
        Equipment.MACHINE,
        "Elliptical machine for low-impact cardio.",
        "Step onto the pedals and grip the moving handles. Begin striding in a smooth elliptical motion. Maintain a steady pace and resistance level. Keep your posture upright and core engaged throughout."
    ),
    Exercise(
        318,
        "Standing Forward Fold",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Standing forward fold to stretch the hamstrings.",
        "Stand tall with your feet hip-width apart. Hinge forward at your hips with a long spine. Reach your hands toward your toes or the floor. Relax your neck and hold the stretch, breathing deeply."
    ),
    Exercise(
        319,
        "Landmine One Arm Bent Over Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Single-arm landmine row targeting the lats and mid-back.",
        "Set one end of the barbell into a landmine or corner. Stand alongside the loaded end and hinge forward at your hips. Grip the sleeve near the plates with one hand. Pull the bar up toward your hip and squeeze your back. Lower it under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),

    Exercise(
        320,
        "Child Pose",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Resting pose that stretches the back and hips.",
        "Kneel on the floor and sit back onto your heels. Fold your torso forward over your thighs. Extend your arms out in front on the ground. Relax and hold the position while breathing slowly."
    ),
    Exercise(
        321,
        "Pigeon Pose",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Hip-opening pose that stretches the glutes.",
        "From all fours, bring one knee forward behind your wrist. Extend the opposite leg straight back behind you. Lower your hips toward the floor. Fold forward over the front leg and hold the stretch."
    ),
    Exercise(
        322,
        "Butterfly Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Seated stretch for the inner thighs and hips.",
        "Sit tall and bring the soles of your feet together. Hold your ankles with both hands. Gently press your knees down toward the floor. Hold the stretch while keeping your back straight."
    ),
    Exercise(
        323,
        "Hip Flexor Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Kneeling stretch for the front of the hip.",
        "Kneel on one knee with the other foot planted in front. Keep your torso upright and core engaged. Push your hips forward until you feel a stretch at the front of the hip. Hold and then switch sides."
    ),
    Exercise(
        324,
        "Quad Stretch (Standing)",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Standing stretch for the quadriceps.",
        "Stand tall and hold onto a support if needed. Bend one knee and grab your ankle behind you. Pull your heel toward your glute. Keep your knees together and hold the stretch before switching sides."
    ),
    Exercise(
        325,
        "Hamstring Stretch (Lying)",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Lying stretch for the hamstrings.",
        "Lie on your back with both legs extended. Raise one leg and hold behind your thigh. Gently pull the leg toward your chest while keeping it fairly straight. Hold the stretch and then switch legs."
    ),
    Exercise(
        326,
        "Calf Stretch (Wall)",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Wall-supported stretch for the calf muscles.",
        "Place both hands on a wall at chest height. Step one foot back and keep that leg straight. Press the back heel down into the floor. Hold the stretch and then switch legs."
    ),
    Exercise(
        327,
        "Chest Stretch (Doorway)",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Doorway stretch for the chest and front of the shoulders.",
        "Stand in a doorway and place your forearm on the frame. Bend your elbow to about ninety degrees. Step forward and lean your weight into the doorway. Feel the stretch across your chest and hold."
    ),
    Exercise(
        328,
        "Shoulder Stretch (Cross-Body)",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Cross-body stretch for the rear shoulder.",
        "Bring one arm straight across your body. Use the opposite hand to hold just above the elbow. Gently pull the arm closer to your chest. Hold the stretch and then switch arms."
    ),
    Exercise(
        329,
        "Lower Back Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Knees-to-chest stretch for the lower back.",
        "Lie flat on your back on the floor. Pull both knees up toward your chest. Wrap your arms around your shins and hold gently. Relax your lower back and breathe through the stretch."
    ),
    Exercise(
        330,
        "330",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Supine spinal twist for spine mobility.",
        "Lie on your back with your arms out to the sides. Bend your knees and bring them toward your chest. Drop both knees to one side while keeping your shoulders down. Turn your head to look the opposite direction and hold, then switch sides."
    ),
    Exercise(
        331,
        "Foam Roll (Upper Back)",
        MuscleGroup.STRETCHING,
        Equipment.OTHER,
        "Foam rolling the upper back for myofascial release.",
        "Lie back with the foam roller under your upper back. Support your head with your hands. Roll slowly up and down along your upper back. Pause on any tight spots and breathe."
    ),
    Exercise(
        332,
        "Foam Roll (IT Band)",
        MuscleGroup.STRETCHING,
        Equipment.OTHER,
        "Foam rolling the IT band for lateral leg release.",
        "Lie on your side with the foam roller under your outer thigh. Support yourself with your forearm and top foot. Roll slowly from hip to knee along the outer thigh. Pause on tender areas and breathe through them."
    ),
    Exercise(
        333,
        "Dynamic Leg Swing",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Dynamic leg swings for hip mobility.",
        "Stand tall and hold a wall or rail for support. Swing one leg forward and backward in a controlled arc. Keep your torso upright and core braced. Perform several swings, then switch legs."
    ),
    Exercise(
        334,
        "World Greatest Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Multi-position stretch for full-body mobility.",
        "Step into a deep forward lunge with one foot. Place both hands on the floor inside the front foot. Rotate your torso and reach one arm toward the ceiling. Return your hand down and straighten the front leg to stretch the hamstring. Repeat on the other side."
    ),
    Exercise(
        335,
        "90-90 Hip Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Seated 90-90 stretch for hip rotation.",
        "Sit on the floor with one leg bent in front at ninety degrees. Position the other leg bent to the side at ninety degrees. Keep your back tall and lean forward over the front shin. Hold the stretch, then switch sides."
    ),
    Exercise(
        336,
        "Archer Pull-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Unilateral pull-up variation for back strength.",
        "Hang from a wide bar with an overhand grip. Pull your body up and toward one hand. Keep the opposite arm extended straight along the bar. Lower under control and alternate sides each rep.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        337,
        "Assisted Chin-Up",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Machine-assisted chin-up for the back and biceps.",
        "Set the assistance weight and kneel on the machine pad. Grip the handles with an underhand shoulder-width grip. Pull your chest up toward the bar. Lower yourself with control until your arms are extended.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        338,
        "Assisted Dip",
        MuscleGroup.TRICEPS,
        Equipment.MACHINE,
        "Machine-assisted dip for the triceps and chest.",
        "Set the assistance weight and kneel or stand on the pad. Grip the parallel handles and support your weight. Lower your body until your upper arms are parallel to the floor. Press back up to full lockout.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        339,
        "Bar Pullover",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Straight-bar pullover targeting the chest and lats.",
        "Lie on a bench holding the bar above your chest with straight arms. Keep a slight bend in your elbows. Lower the bar in an arc back behind your head. Pull it back over your chest using your chest and lats.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.TRICEPS)
    ),
    Exercise(
        340,
        "Barbell Box Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Box squat with a barbell for quad strength and depth control.",
        "Set a box behind you and position the barbell across your upper back. Sit back and down until you lightly touch the box. Pause briefly without relaxing. Drive up powerfully through your heels to stand.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        341,
        "Barbell Front Raise",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Barbell front raise for the anterior deltoid.",
        "Stand holding the barbell against your thighs with an overhand grip. Keep your arms nearly straight. Raise the bar forward to shoulder height. Lower it back down under control."
    ),
    Exercise(
        342,
        "Barbell Guillotine Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Upper chest press with the bar lowered toward the neck.",
        "Lie on a flat bench with a wide grip on the bar. Lower the barbell slowly toward your upper chest near your neck. Keep your elbows flared out. Press the bar back up to lockout.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        343,
        "Barbell Incline Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Chest-supported incline bench row for upper back thickness.",
        "Lie chest-down on an incline bench holding a barbell. Let your arms hang straight toward the floor. Row the bar up toward your chest while squeezing your shoulder blades. Lower it under control.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        344,
        "Barbell Larsen Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Bench press with feet elevated to remove leg drive.",
        "Lie on a flat bench and raise your feet off the floor. Grip the barbell slightly wider than shoulder width. Lower the bar under control to your mid-chest. Press it back up to full lockout while keeping your core tight.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        345,
        "Barbell Pullover",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Classic barbell pullover for chest and lats.",
        "Lie across a flat bench with only your upper back supported. Hold the barbell over your chest with arms slightly bent. Lower the bar in an arc behind your head until you feel a stretch. Pull it back over your chest while squeezing your chest and lats.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.TRICEPS)
    ),
    Exercise(
        346,
        "Barbell Walking Lunge",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Walking lunge with a barbell for leg development.",
        "Rest the barbell across your upper back and stand tall. Step forward into a lunge until both knees are bent about ninety degrees. Drive through your front heel to bring your back foot forward. Continue alternating legs as you walk forward.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        347,
        "Behind-the-Neck Press",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Barbell overhead press performed from behind the neck.",
        "Sit upright with the barbell resting behind your neck on your traps. Grip the bar wider than shoulder width. Press the bar straight overhead until your arms lock out. Lower it back behind your neck under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        348,
        "Glute Bridge (Bodyweight)",
        MuscleGroup.GLUTES,
        Equipment.BODYWEIGHT,
        "Bodyweight glute bridge for glute activation.",
        "Lie on your back with knees bent and feet flat on the floor. Drive your hips upward by squeezing your glutes. Form a straight line from knees to shoulders at the top. Lower your hips slowly back to the floor.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        349,
        "Cable Romanian Deadlift",
        MuscleGroup.HAMSTRINGS,
        Equipment.CABLE,
        "Cable RDL for a hamstring and glute stretch.",
        "Stand facing a low cable holding the handle with both hands. Hinge at your hips and push them back while keeping your back flat. Lower your torso until you feel a stretch in your hamstrings. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        350,
        "Cable Concentration Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Single-arm cable curl for the bicep peak.",
        "Sit or stand beside a low cable pulley with one arm. Brace your elbow against your inner thigh or keep it pinned at your side. Curl the handle upward toward your shoulder. Squeeze the bicep at the top, then lower slowly."
    ),
    Exercise(
        351,
        "Cable Shrug",
        MuscleGroup.TRAPS,
        Equipment.CABLE,
        "Cable shrug for constant trap tension.",
        "Hold the cable handles at your sides with arms straight. Keep your chest up and shoulders relaxed to start. Shrug your shoulders straight up toward your ears. Hold briefly at the top, then lower under control."
    ),
    Exercise(
        352,
        "Cable Wrist Extension",
        MuscleGroup.FOREARMS,
        Equipment.CABLE,
        "Cable wrist extension for the forearm extensors.",
        "Hold a low cable behind your back with an overhand grip. Keep your arm straight and still. Extend your wrist upward against the resistance. Lower slowly back to the starting position."
    ),
    Exercise(
        353,
        "Cable Step-Up",
        MuscleGroup.QUADS,
        Equipment.CABLE,
        "Cable-resisted step-up for single-leg quad work.",
        "Attach a cable to your ankle and stand beside a sturdy box. Place the loaded leg on top of the box. Drive through that heel to step up onto the box. Lower under control and repeat before switching legs.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        354,
        "Cable Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.CABLE,
        "Cable wrist curl for the forearm flexors.",
        "Sit with your forearm resting on your knee holding a low cable. Keep your palm facing upward. Curl your wrist upward against the resistance. Lower the handle slowly to stretch the forearm."
    ),
    Exercise(
        355,
        "Dumbbell Hex Press",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Inner-chest press pressing the dumbbells together.",
        "Lie on a flat bench holding two dumbbells pressed firmly together. Keep them in contact over your chest. Lower them to your chest while maintaining the squeeze. Press back up while continuing to push the dumbbells together.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        356,
        "Dumbbell Larsen Press",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Feet-elevated dumbbell press emphasizing the chest.",
        "Lie on a flat bench with your feet raised off the ground. Hold a dumbbell in each hand over your chest. Lower the dumbbells to chest level under control. Press them back up to full extension without driving with your legs.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        357,
        "Dumbbell Wrist Extension",
        MuscleGroup.FOREARMS,
        Equipment.DUMBBELL,
        "Standing dumbbell wrist extension for the extensors.",
        "Stand holding a dumbbell behind your back with an overhand grip. Keep your arm straight throughout. Extend your wrist upward against gravity. Lower it slowly back down."
    ),
    Exercise(
        358,
        "Dumbbell Wrist Extension (Over Knee)",
        MuscleGroup.FOREARMS,
        Equipment.DUMBBELL,
        "Seated wrist extension over the knee for the extensors.",
        "Sit and rest your forearm on your knee with your palm facing down. Hold a dumbbell with an overhand grip. Extend your wrist upward as high as possible. Lower it slowly to stretch the extensors."
    ),
    Exercise(
        359,
        "EZ-Bar Pullover",
        MuscleGroup.CHEST,
        Equipment.EZ_BAR,
        "EZ-bar pullover for a chest and lat stretch.",
        "Lie on a flat bench holding the EZ-bar over your chest. Keep your arms slightly bent. Lower the bar in an arc behind your head until you feel a stretch. Pull it back over your chest under control.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.TRICEPS)
    ),
    Exercise(
        360,
        "Katana Extension",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Cable tricep extension with a diagonal pulling arc.",
        "Hold a cable handle up beside your head with one arm. Keep your upper arm stable. Extend your arm downward in a diagonal arc like drawing a sword. Return slowly to the start and repeat."
    ),
    Exercise(
        361,
        "Reverse Cable Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.CABLE,
        "Reverse cable wrist curl for the forearm extensors.",
        "Sit with your forearm on your knee holding a low cable overhand. Let your wrist drop toward the floor. Curl your wrist upward against the resistance. Lower it slowly back down."
    ),
    Exercise(
        362,
        "Reverse Grip Bench Press",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Reverse-grip bench press emphasizing the upper chest.",
        "Lie on a flat bench gripping the bar with a supinated underhand grip. Unrack the bar and hold it over your chest. Lower it under control to your lower chest. Press it back up to full lockout.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        363,
        "Reverse Plank",
        MuscleGroup.LOWER_BACK,
        Equipment.BODYWEIGHT,
        "Reverse plank for lower-back and glute endurance.",
        "Sit on the floor with your legs extended in front of you. Place your hands behind your hips with fingers pointing forward. Lift your hips until your body forms a straight line. Hold the position while squeezing your glutes.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.ABS)
    ),
    Exercise(
        364,
        "Ski Erg",
        MuscleGroup.CARDIO,
        Equipment.OTHER,
        "Ski erg machine for full-body cardio.",
        "Stand facing the machine and grip the handles overhead. Hinge at your hips as you drive the handles down. Pull them past your hips in a double-pole skiing motion. Return to the top and repeat in a steady rhythm.",
        secondaryMuscles = listOf(MuscleGroup.BACK, MuscleGroup.ABS)
    ),
    Exercise(
        365,
        "45-Degree Incline Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Chest-supported incline row for mid-back thickness.",
        "Lie chest-down on a bench set to a forty-five degree incline. Let the dumbbells hang straight down with arms extended. Row them up toward your hips while squeezing your shoulder blades. Lower them slowly back to the stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        366,
        "5-Dot Drill",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Agility dot drill for coordination and quickness.",
        "Set up five dots in an X pattern on the floor. Jump between the dots in the prescribed sequence as fast as possible. Land lightly on the balls of your feet. Keep your knees soft and maintain a quick rhythm."
    ),
    Exercise(
        367,
        "Arm Circles",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Arm circles for shoulder warm-up and mobility.",
        "Stand tall and extend your arms straight out to your sides. Begin making small forward circles. Gradually increase the circles to a larger size. Reverse the direction and repeat backward."
    ),
    Exercise(
        368,
        "Arm Scissors",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Cross-body arm swings for shoulder and chest mobility.",
        "Stand tall and extend your arms out to your sides. Swing them across your body so they overlap. Open them back out to the sides. Repeat while alternating which arm crosses on top."
    ),
    Exercise(
        369,
        "Barbell JM Press",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "JM press hybrid movement for tricep mass.",
        "Lie on a flat bench holding the barbell over your chest. Lower the bar toward your neck while keeping your elbows tucked forward. Stop when your forearms approach your biceps. Press the bar back up by driving with your triceps."
    ),
    Exercise(
        370,
        "Barbell Seated Calf Raise",
        MuscleGroup.CALVES,
        Equipment.BARBELL,
        "Seated calf raise with a barbell across the knees.",
        "Sit on a bench with the balls of your feet on a raised platform. Rest a barbell across your lower thighs. Raise your heels by pushing up onto your toes. Squeeze your calves at the top, then lower slowly."
    ),
    Exercise(
        371,
        "Bear Crawl",
        MuscleGroup.FULL_BODY,
        Equipment.BODYWEIGHT,
        "Bear crawl for full-body conditioning and core stability.",
        "Start on your hands and feet with your knees hovering off the floor. Keep your back flat and core braced. Crawl forward by moving opposite hand and foot together. Continue while keeping your hips low and stable.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ABS, MuscleGroup.QUADS)
    ),
    Exercise(
        372,
        "Behind-the-Back Barbell Shrug",
        MuscleGroup.TRAPS,
        Equipment.BARBELL,
        "Reverse barbell shrug held behind the body for the traps.",
        "Stand holding a barbell behind your hips with an overhand grip. Keep your arms straight and chest up. Shrug your shoulders straight up toward your ears. Hold briefly, then lower the bar under control."
    ),
    Exercise(
        373,
        "Dumbbell Bent-Over Row",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Two-arm bent-over row with dumbbells for back thickness.",
        "Hinge at your hips with a flat back and a dumbbell in each hand. Let the dumbbells hang straight down. Pull both dumbbells up to the sides of your torso. Squeeze your shoulder blades, then lower under control.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        374,
        "Body Saw Plank",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Dynamic plank for anti-extension core strength.",
        "Set up in a forearm plank with your feet on sliders or a smooth surface. Brace your core and keep your hips level. Slide your body forward and backward using your shoulders. Keep the movement controlled and your spine neutral."
    ),
    Exercise(
        375,
        "Bodyweight Plie Squat",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Wide-stance plie squat for the inner thighs and quads.",
        "Stand with your feet wide and your toes turned outward. Keep your chest up and core engaged. Squat straight down while keeping your knees tracking over your toes. Drive through your heels to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        376,
        "Brisk Walk",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Brisk walking for low-impact cardiovascular conditioning.",
        "Walk at a fast, purposeful pace. Maintain an upright posture with your chest up. Swing your arms naturally in time with your stride. Keep your breathing steady throughout the session."
    ),
    Exercise(
        377,
        "Burpee Long Jump",
        MuscleGroup.FULL_BODY,
        Equipment.BODYWEIGHT,
        "Burpee with a forward long jump for explosive conditioning.",
        "Drop into a squat and kick your feet back into a plank. Jump your feet back toward your hands. Explode upward into a forward long jump. Land softly with bent knees and reset for the next rep.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.CHEST)
    ),
    Exercise(
        378,
        "Burpee with Push-Up",
        MuscleGroup.FULL_BODY,
        Equipment.BODYWEIGHT,
        "Burpee including a full push-up for upper-body engagement.",
        "Drop down and kick your feet back into a push-up position. Perform a full push-up to the floor. Jump your feet back in toward your hands. Explode upward into a jump and reset.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.QUADS)
    ),
    Exercise(
        379,
        "Cable Behind-the-Neck Pulldown",
        MuscleGroup.BACK,
        Equipment.CABLE,
        "Lat pulldown to behind the neck for upper-lat width.",
        "Sit at the pulldown station and grip the bar wider than shoulder width. Keep your torso upright. Pull the bar down behind your head to neck level. Return it to the top under control.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        380,
        "Single-Arm Cable Hammer Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Unilateral cable curl with a neutral grip for the brachialis.",
        "Stand sideways to a low cable holding the handle with a neutral grip. Keep your elbow pinned at your side. Curl the handle up toward your shoulder. Lower it slowly back to the start."
    ),
    Exercise(
        381,
        "Calf Raise (Resistance Band)",
        MuscleGroup.CALVES,
        Equipment.RESISTANCE_BAND,
        "Standing calf raise using a resistance band.",
        "Stand on the middle of the band and hold the handles at your shoulders. Keep your legs straight and core engaged. Raise up onto your toes as high as possible. Squeeze your calves, then lower slowly."
    ),
    Exercise(
        382,
        "Depth Jump to Hurdle Hop",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Plyometric depth jump into a hurdle hop for reactive power.",
        "Step off a box and land softly on both feet. Immediately explode upward to jump over the hurdle. Minimize your ground contact time between the landing and the jump. Reset and repeat with full recovery.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES)
    ),
    Exercise(
        383,
        "Dumbbell Cossack Squat",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Lateral squat holding a dumbbell for mobility and leg strength.",
        "Stand with a wide stance holding a dumbbell at your chest. Shift your weight to one side and squat deeply. Keep the opposite leg straight with the toes up. Push back to center and repeat on the other side.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        384,
        "Dumbbell Deadlift",
        MuscleGroup.BACK,
        Equipment.DUMBBELL,
        "Standard deadlift pattern performed with dumbbells.",
        "Stand with a dumbbell on each side of your feet. Hinge your hips back and bend down to grip the handles. Keep your back flat as you drive through your heels. Stand up fully, then lower the dumbbells under control.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        385,
        "Dumbbell Decline Shrug",
        MuscleGroup.TRAPS,
        Equipment.DUMBBELL,
        "Shrug on a decline bench targeting the lower trap fibers.",
        "Lie face-down on a decline bench holding a dumbbell in each hand. Let your arms hang straight down. Shrug your shoulders upward and back. Hold briefly, then lower under control."
    ),
    Exercise(
        386,
        "Dumbbell Good Morning",
        MuscleGroup.HAMSTRINGS,
        Equipment.DUMBBELL,
        "Good morning with a dumbbell held at the chest for the hamstrings.",
        "Hold a dumbbell against your chest with both hands. Keep your knees slightly bent and your back flat. Hinge forward at your hips until you feel a hamstring stretch. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        387,
        "Dumbbell Incline Shrug",
        MuscleGroup.TRAPS,
        Equipment.DUMBBELL,
        "Shrug on an incline bench for upper-trap emphasis.",
        "Lie face-down on an incline bench holding a dumbbell in each hand. Let your arms hang straight toward the floor. Shrug your shoulders up toward your ears. Hold briefly, then lower slowly."
    ),
    Exercise(
        388,
        "Dumbbell Jefferson Curl",
        MuscleGroup.HAMSTRINGS,
        Equipment.DUMBBELL,
        "Weighted spinal flexion drill that stretches the hamstrings and posterior chain.",
        "Stand tall on a raised platform holding a dumbbell in front of you. Tuck your chin and slowly roll your spine down one vertebra at a time. Let the dumbbell hang as you feel the stretch through your hamstrings. Reverse the motion and stack your spine back up to standing.",
        secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.GLUTES)
    ),
    Exercise(
        389,
        "Dumbbell Push Press",
        MuscleGroup.SHOULDERS,
        Equipment.DUMBBELL,
        "Overhead press driven by the legs to move heavier dumbbells.",
        "Hold a dumbbell at each shoulder with your feet hip width apart. Dip slightly by bending your knees. Drive up explosively through your legs and press the dumbbells overhead. Lower them back to your shoulders under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.QUADS)
    ),
    Exercise(
        390,
        "Gittleson Shrug",
        MuscleGroup.TRAPS,
        Equipment.DUMBBELL,
        "Seated diagonal shrug that targets the upper traps.",
        "Sit on a bench holding a dumbbell in each hand. Shrug your shoulders forward and upward in a smooth circular motion. Squeeze the traps at the top of the movement. Lower back down and repeat with control."
    ),
    Exercise(
        391,
        "Dumbbell Seated Neutral Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.DUMBBELL,
        "Seated wrist curl with a neutral grip for the forearm flexors.",
        "Sit and rest your forearm along your thigh with a neutral grip on the dumbbell. Let your wrist drop down toward the floor. Curl the wrist upward and squeeze the forearm. Lower back down slowly and repeat."
    ),
    Exercise(
        392,
        "Dumbbell Sumo Deadlift",
        MuscleGroup.HAMSTRINGS,
        Equipment.DUMBBELL,
        "Wide-stance deadlift with a dumbbell held between the legs.",
        "Stand with a wide stance and your toes turned out. Grip a dumbbell with both hands between your legs. Hinge at the hips and bend your knees to lower it. Drive your hips forward to stand tall and lock out.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.QUADS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        393,
        "Dumbbell Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.DUMBBELL,
        "Classic wrist curl with a dumbbell for the forearm flexors.",
        "Rest your forearm on a bench with your palm facing up. Hold a dumbbell and let your wrist extend down. Curl the wrist upward while squeezing the forearm. Lower back to the start under control."
    ),
    Exercise(
        394,
        "Full Planche",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Advanced static hold with the body held horizontal and parallel to the ground.",
        "Place your hands on the floor with fingers spread for stability. Lean your weight forward over your hands. Lift your feet and extend your legs straight behind you. Hold a rigid horizontal body line for the desired time.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        395,
        "Hack Machine Single-Leg Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Single-leg calf raise performed on the hack squat machine.",
        "Position the ball of one foot on the edge of the hack squat platform. Let your heel drop to stretch the calf. Press up onto your toes as high as possible. Lower under control and repeat before switching legs."
    ),
    Exercise(
        396,
        "Hack Squat Calf Raise",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Calf raise performed on the hack squat machine.",
        "Place the balls of both feet on the bottom edge of the hack squat platform. Let your heels drop to stretch the calves. Press up onto your toes as high as you can. Lower slowly and repeat for reps."
    ),
    Exercise(
        397,
        "Heel-Elevated Goblet Squat",
        MuscleGroup.QUADS,
        Equipment.DUMBBELL,
        "Goblet squat with elevated heels for increased quad activation.",
        "Hold a dumbbell vertically against your chest. Place your heels on a plate or wedge for elevation. Squat down while keeping your torso upright. Drive through your feet to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        398,
        "Band-Assisted Muscle-Up",
        MuscleGroup.BACK,
        Equipment.RESISTANCE_BAND,
        "Muscle-up performed with band assistance as a progression.",
        "Loop a resistance band over the bar and place your feet or knees in it. Grip the bar and pull explosively toward your chest. Transition your wrists over the bar as you rise. Press up to full lockout above the bar.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        399,
        "Incline Chest Fly Machine",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Machine fly on an incline setting to isolate the upper chest.",
        "Set the machine to an incline and sit with your back on the pad. Grip the handles with a slight bend in your elbows. Bring your arms together in a wide arc in front of you. Return slowly to feel the stretch across the upper chest."
    ),
    Exercise(
        400,
        "Lever Shrug",
        MuscleGroup.TRAPS,
        Equipment.MACHINE,
        "Shrug performed on a lever machine for trap development.",
        "Stand or sit at the lever machine and grip the handles. Keep your arms straight and your posture tall. Shrug your shoulders straight up toward your ears. Lower under control and repeat."
    ),
    Exercise(
        401,
        "Weighted Lateral Neck Flexion",
        MuscleGroup.STRETCHING,
        Equipment.DUMBBELL,
        "Weighted side-to-side neck flexion for neck strength.",
        "Lie on your side on a bench with your head off the edge. Hold a small weight against the side of your head near your temple. Lower your head toward the floor under control. Raise it back up sideways and repeat."
    ),
    Exercise(
        402,
        "Medicine Ball Rotational Throw",
        MuscleGroup.FULL_BODY,
        Equipment.MEDICINE_BALL,
        "Explosive rotational medicine ball throw for core power.",
        "Stand sideways to a wall holding a medicine ball at your midsection. Rotate your hips and torso away from the wall to wind up. Explosively rotate back and throw the ball against the wall. Catch the rebound and reset for the next rep.",
        secondaryMuscles = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS, MuscleGroup.GLUTES)
    ),
    Exercise(
        403,
        "Single-Arm Reverse Tricep Pushdown",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Single-arm cable pushdown with an underhand grip for the triceps.",
        "Face the cable and grasp the handle with one hand using an underhand grip. Keep your elbow tucked at your side. Push the handle down until your arm is fully extended. Squeeze the triceps then return under control."
    ),
    Exercise(
        404,
        "Single-Arm Single-Leg Bench Dip",
        MuscleGroup.TRICEPS,
        Equipment.BODYWEIGHT,
        "Advanced bench dip balanced on one arm and one leg.",
        "Place one hand on the edge of a bench behind you. Extend one leg out straight in front of you. Lower your body by bending the supporting elbow. Press back up to full extension and repeat.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        405,
        "Overhead Shrug",
        MuscleGroup.TRAPS,
        Equipment.BARBELL,
        "Shrug with the barbell held overhead to target the upper traps.",
        "Press a barbell overhead and hold it with straight arms. Keep your core braced and posture tall. Shrug your shoulders upward toward your ears. Lower back down and repeat with control."
    ),
    Exercise(
        406,
        "Pendulum Lunge",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Continuous lunge alternating forward and reverse without resetting.",
        "Step forward into a front lunge and lower until both knees bend. Push back off the front foot and swing that leg into a reverse lunge. Flow continuously between the forward and reverse positions. Keep your torso upright throughout the pendulum motion.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        407,
        "Plate Push",
        MuscleGroup.FULL_BODY,
        Equipment.PLATE,
        "Pushing a weight plate along the floor for full-body conditioning.",
        "Place a weight plate on a smooth floor surface. Get into a low athletic stance with your hips down. Drive through your legs to push the plate forward. Keep moving for the prescribed distance or time.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        408,
        "Power Lunge",
        MuscleGroup.QUADS,
        Equipment.BODYWEIGHT,
        "Explosive plyometric lunge for leg power.",
        "Lower into a lunge with your front thigh near parallel. Drive explosively off your front foot to jump up. Switch legs or land on the same leg as desired. Absorb the landing softly and repeat.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES)
    ),
    Exercise(
        409,
        "PVC Hip Hinge",
        MuscleGroup.LOWER_BACK,
        Equipment.OTHER,
        "Hip hinge drill using a PVC pipe to groove proper technique.",
        "Hold a PVC pipe vertically along your spine touching your head, upper back, and tailbone. Push your hips back while keeping the three contact points. Hinge until you feel a stretch in your hamstrings. Drive your hips forward to return to standing.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES)
    ),
    Exercise(
        410,
        "Rotating Neck Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Gentle neck rotation stretch for cervical mobility.",
        "Sit or stand tall with relaxed shoulders. Slowly rotate your head to one side until you feel a gentle stretch. Hold briefly then return to center. Repeat the rotation to the other side."
    ),
    Exercise(
        411,
        "Seated Back Extension",
        MuscleGroup.LOWER_BACK,
        Equipment.MACHINE,
        "Seated machine back extension for lower back strength.",
        "Sit in the machine with your upper back against the pad. Adjust the settings so you are positioned comfortably. Extend your torso backward against the resistance. Return to the start under control and repeat."
    ),
    Exercise(
        412,
        "Seated Barbell Finger Curl",
        MuscleGroup.FOREARMS,
        Equipment.BARBELL,
        "Finger curl with a barbell for grip and forearm strength.",
        "Sit with your forearms on your thighs and the barbell in your fingertips. Let the bar roll down to your fingers. Curl your fingers up to roll the bar into your palms. Lower it back to the fingertips and repeat."
    ),
    Exercise(
        413,
        "Shadow Boxing",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Shadow boxing for cardio conditioning and coordination.",
        "Stand in a boxing stance with your hands up. Throw combinations of punches at an imaginary opponent. Add slips, footwork, and head movement between strikes. Keep a steady pace to maintain your heart rate."
    ),
    Exercise(
        414,
        "Side Neck Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Lateral neck stretch for cervical flexibility.",
        "Sit or stand tall with relaxed shoulders. Tilt your head toward one shoulder until you feel a stretch. Hold the position for several breaths. Return to center and repeat on the opposite side."
    ),
    Exercise(
        415,
        "Copenhagen Hip Adduction",
        MuscleGroup.GLUTES,
        Equipment.BODYWEIGHT,
        "Side plank adduction targeting the inner thigh and glute medius.",
        "Support yourself on one forearm with your top foot resting on a bench. Lift your hips so your body forms a straight line. Raise your bottom leg up toward the bench. Hold or pulse, then switch sides.",
        secondaryMuscles = listOf(MuscleGroup.ABS)
    ),
    Exercise(
        416,
        "Side Push Neck Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Assisted lateral neck stretch for a deeper cervical release.",
        "Sit or stand tall and tilt your head to one side. Place your same-side hand over the top of your head. Gently guide your head further into the stretch. Hold briefly then release and repeat on the other side."
    ),
    Exercise(
        417,
        "Single-Leg Dumbbell Side Bridge",
        MuscleGroup.ABS,
        Equipment.DUMBBELL,
        "Weighted single-leg side plank for the obliques and core stability.",
        "Set up in a side plank on your forearm with your body in a straight line. Lift your top foot off the bottom one to balance on a single leg. Rest a dumbbell on your top hip for added resistance. Hold the position, then switch sides."
    ),
    Exercise(
        418,
        "Smith Machine Behind-the-Neck Press",
        MuscleGroup.SHOULDERS,
        Equipment.SMITH_MACHINE,
        "Behind-the-neck overhead press performed on the Smith machine.",
        "Sit under the Smith machine bar with it resting behind your neck. Grip the bar wider than shoulder width. Press it straight up to full extension. Lower it back to the base of your neck under control.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        419,
        "Standing Barbell Concentration Curl",
        MuscleGroup.BICEPS,
        Equipment.BARBELL,
        "Standing concentration curl with a barbell to build the bicep peak.",
        "Stand bent at the hips holding a barbell with one or both hands. Brace your elbow against your inner thigh or torso. Curl the bar up while squeezing the bicep. Lower it slowly to full extension and repeat."
    ),
    Exercise(
        420,
        "Stomach Vacuum",
        MuscleGroup.ABS,
        Equipment.BODYWEIGHT,
        "Isometric core drill targeting the transverse abdominis.",
        "Stand or kneel tall and exhale all of your air out. Draw your navel in toward your spine as far as possible. Hold the contraction for several seconds while breathing shallowly. Relax and repeat for the desired reps."
    ),
    Exercise(
        421,
        "Step-Behind Rotational Med Ball Throw",
        MuscleGroup.FULL_BODY,
        Equipment.MEDICINE_BALL,
        "Rotational power throw with step-behind footwork for explosiveness.",
        "Stand sideways to a wall holding a medicine ball. Step one foot behind the other to load your hips. Rotate powerfully and throw the ball against the wall. Catch the rebound and reset for the next throw.",
        secondaryMuscles = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS, MuscleGroup.GLUTES)
    ),
    Exercise(
        422,
        "Top Half Pull-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Partial pull-up worked through the upper range of motion.",
        "Pull yourself up until your chin is over the bar. Lower yourself only halfway down. Pull back up to the top of the range. Keep your reps within the upper half of the movement.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS)
    ),
    Exercise(
        423,
        "Unilateral Lat Wall Stretch",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Single-arm lat stretch using a wall for support.",
        "Stand facing a wall at a slight angle. Extend one arm overhead and place your hand against the wall. Shift your hips away from the wall to deepen the stretch. Hold, then switch to the other side."
    ),
    Exercise(
        424,
        "Waiter Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Bicep curl holding a plate flat like a waiter's tray.",
        "Hold a weight plate flat in both palms with your fingers spread. Keep your elbows in front of your body. Curl the plate up toward your shoulders. Lower it slowly while focusing on the bicep squeeze."
    ),
    Exercise(
        425,
        "Walking",
        MuscleGroup.CARDIO,
        Equipment.BODYWEIGHT,
        "Walking for general cardiovascular health.",
        "Stand tall with an upright posture. Walk at a comfortable steady pace. Let your arms swing naturally at your sides. Maintain your rhythm for the desired duration."
    ),
    Exercise(
        426,
        "Weighted Lying Neck Extension",
        MuscleGroup.STRETCHING,
        Equipment.DUMBBELL,
        "Weighted neck extension for posterior neck strength.",
        "Lie face down on a bench with your head off the edge. Hold a small weight against the back of your head. Lower your head down toward the floor under control. Extend your neck to raise your head back up."
    ),
    Exercise(
        427,
        "Weighted Lying Neck Flexion",
        MuscleGroup.STRETCHING,
        Equipment.DUMBBELL,
        "Weighted neck flexion for anterior neck strength.",
        "Lie face up on a bench with your head off the edge. Hold a small weight against your forehead. Lower your head back under control. Flex your neck to curl your chin toward your chest."
    ),
    Exercise(
        428,
        "Weighted Muscle-Up",
        MuscleGroup.BACK,
        Equipment.BODYWEIGHT,
        "Muscle-up with added weight for an advanced progression.",
        "Wear a weight belt or vest and hang from the bar. Pull explosively toward your chest. Transition your wrists over the bar as you rise. Press up to full extension above the bar.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.CHEST)
    ),
    Exercise(
        429,
        "Wrist Circles",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Wrist circle rotations for wrist mobility.",
        "Extend your arms straight out in front of you. Make large circles with your wrists. Rotate in one direction for several reps. Reverse and circle in the opposite direction."
    ),
    Exercise(
        430,
        "Wrist Rotations",
        MuscleGroup.STRETCHING,
        Equipment.BODYWEIGHT,
        "Wrist rotation drill to warm up the forearms and wrists.",
        "Hold your arms out in front of you. Rotate your wrists through their full range of motion. Turn them clockwise for several reps. Then rotate counterclockwise to finish."
    ),
    Exercise(
        431,
        "Cable Seated Chest Press",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Seated cable press for building the chest with constant tension.",
        "Sit between the cables with your back supported. Grip a handle in each hand at chest height. Press the handles forward until your arms are nearly straight. Bring them together in an arc and squeeze your chest. Return under control to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        432,
        "Iso-lateral Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Plate-loaded row for independent unilateral back development.",
        "Sit facing the chest pad with your feet planted. Grab the handles with a firm grip. Pull back while driving your elbows downward and back. Squeeze your shoulder blades together at the end. Return slowly to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        433,
        "Machine Seated Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Seated row machine for mid-back development.",
        "Sit facing the machine with your chest against the pad. Grip the handles with both hands. Pull toward your torso while squeezing your shoulder blades together. Return under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        434,
        "Machine Unilateral Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Single-arm machine row for isolated lat and mid-back development.",
        "Sit facing the machine with your chest supported. Grip one handle with a neutral grip. Pull toward your torso to work one side at a time. Squeeze your back at the peak. Lower the weight slowly and repeat on the other side.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        435,
        "Machine Standing T-Bar Grip Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "Standing T-bar row emphasizing thickness in the mid-back.",
        "Straddle the machine platform with knees slightly bent. Hinge forward and grip the handles securely. Pull the weight toward your torso. Squeeze your shoulder blades together at the top. Lower under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        436,
        "Machine Standing T-Bar Reverse Grip Row",
        MuscleGroup.BACK,
        Equipment.MACHINE,
        "T-bar row using an underhand grip to bias the lower lats.",
        "Straddle the machine platform with knees slightly bent. Take a reverse underhand grip on the handles. Pull the weight toward your waist. Squeeze your back at the top of the movement. Lower slowly to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        437,
        "Cable Seated Incline Chest Press",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Incline cable press emphasizing the upper chest.",
        "Position a bench between the cables and set it to an incline. Sit back and grip a handle in each hand. Press the handles forward and upward until they meet in the center. Squeeze your upper chest at the top. Lower under control to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        438,
        "Machine One Arm Side Chest Press",
        MuscleGroup.CHEST,
        Equipment.MACHINE,
        "Unilateral chest press performed seated to isolate the pectorals.",
        "Sit sideways in the machine with your torso aligned to the handle. Grip the handle firmly with one hand. Press across your body until your arm is nearly straight. Squeeze your chest at the end of the press. Return slowly and repeat on the other side.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        439,
        "Landmine Floor One Arm Chest Fly",
        MuscleGroup.CHEST,
        Equipment.BARBELL,
        "Single-arm chest fly on the floor using a landmine attachment.",
        "Lie flat on the floor next to the loaded barbell. Hold the sleeve with one hand and a slight bend in your elbow. Arc the weight inward across your chest. Squeeze your pec at the top. Lower under control to the side.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS)
    ),
    Exercise(
        440,
        "Cable Seated Chest Fly",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Seated chest fly on a bench using a cable machine.",
        "Sit on a bench positioned between the cables. Grasp a handle in each hand with a slight elbow bend. Bring your hands together in front of your chest in a wide arc. Squeeze your chest at the peak. Return slowly to a full stretch."
    ),
    Exercise(
        441,
        "Cable Fly with Chest Supported",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Chest-supported cable fly that eliminates momentum for strict isolation.",
        "Lean your chest against an inclined bench pad facing away from the cables. Hold a cable handle in each hand. Sweep your arms forward in a wide arc until they meet. Squeeze your chest at the top. Return under control to a stretch."
    ),
    Exercise(
        442,
        "Cable One Arm Decline Chest Fly",
        MuscleGroup.CHEST,
        Equipment.CABLE,
        "Single-arm decline chest fly performed lying on a bench with a cable.",
        "Lie on a decline bench positioned next to the pulley. Grip the handle with one hand and keep a slight elbow bend. Sweep your arm inward and across your chest. Squeeze your pec at the top. Lower slowly and repeat on the other side."
    ),
    Exercise(
        443,
        "Dumbbell One Arm Chest Fly on Exercise Ball",
        MuscleGroup.CHEST,
        Equipment.DUMBBELL,
        "Single-arm dumbbell chest fly performed while balancing on an exercise ball.",
        "Lie with your upper back supported on the ball and hips raised. Hold a dumbbell in one hand above your chest. Lower it out to the side in a wide arc with a slight elbow bend. Squeeze your chest as you raise it back up. Repeat on the other side."
    ),
    Exercise(
        444,
        "Band Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.RESISTANCE_BAND,
        "Resistance band shrug targeting the upper trapezius.",
        "Stand on the middle of the band with feet shoulder width apart. Grasp the handles or ends at your sides. Elevate your shoulders toward your ears. Hold the contraction briefly. Lower slowly to the start."
    ),
    Exercise(
        445,
        "Barbell Seated Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Seated barbell shrug that isolates the upper traps by removing leg drive.",
        "Sit on a bench with your feet flat on the floor. Hold the barbell across your thighs with an overhand grip. Elevate your shoulders toward your ears. Pause and squeeze your traps at the top. Lower the bar under control."
    ),
    Exercise(
        446,
        "Smith Shrug Behind the Back",
        MuscleGroup.SHOULDERS,
        Equipment.MACHINE,
        "Smith machine shrug with the bar behind the back to target the upper traps.",
        "Stand facing away from the Smith machine bar. Grip the bar behind your glutes with an overhand grip. Shrug your shoulders straight up toward your ears. Squeeze your traps at the top. Lower the bar slowly to the start."
    ),
    Exercise(
        447,
        "Cable Lying Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Lying cable shrug performed on a bench to isolate the trapezius.",
        "Lie flat on your back facing the cable pulley. Grip the bar handle with both hands and arms extended. Pull your shoulder blades upward toward your ears. Hold the squeeze briefly. Return under control to a stretch."
    ),
    Exercise(
        448,
        "Cable Seated Horizontal Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.CABLE,
        "Seated horizontal cable shrug targeting the mid-traps and rhomboids.",
        "Sit facing the cable column with your feet braced. Hold the handles with arms extended in front of you. Retract your shoulder blades straight back. Squeeze your mid-back at the end. Return slowly to a full stretch."
    ),
    Exercise(
        449,
        "Bodyweight Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.BODYWEIGHT,
        "Bodyweight shrug from dip bars to isolate the shoulder girdle.",
        "Suspend yourself on dip bars with your arms straight. Allow your shoulders to shrug upward toward your ears. Press through your palms to depress your shoulders. Hold the bottom position briefly. Repeat in a controlled rhythm."
    ),
    Exercise(
        450,
        "Kettlebell Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.KETTLEBELL,
        "Kettlebell shrug developing the upper trapezius with a neutral grip.",
        "Hold a kettlebell in each hand at your sides. Stand upright with your core braced. Elevate your shoulders toward your ears. Squeeze your traps at the top. Lower the kettlebells slowly to the start."
    ),
    Exercise(
        451,
        "Machine Gripless Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.MACHINE,
        "Machine shrug using pads to isolate the traps without grip fatigue.",
        "Place your forearms or shoulders against the machine pads. Stand tall with your core braced. Elevate your shoulders toward your ears. Hold the contraction at the top. Lower the weight under control."
    ),
    Exercise(
        452,
        "Hanging Scapular Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.BODYWEIGHT,
        "Bodyweight scapular shrug hanging from a bar to build scapular stability.",
        "Hang from a pull-up bar with your arms straight. Keep your arms locked and pull your shoulder blades down and back. Allow your shoulders to rise again at the top. Repeat the scapular movement under control. Keep your core engaged throughout."
    ),
    Exercise(
        453,
        "Inverted Shrug",
        MuscleGroup.SHOULDERS,
        Equipment.BODYWEIGHT,
        "Bodyweight shrug performed from an inverted row position.",
        "Set up under a bar or straps with your body in a straight line. Keep your arms straight and your heels on the floor. Shrug your shoulder blades together to raise your torso. Hold the contraction briefly. Lower under control to the start."
    ),
    Exercise(
        454,
        "Jump Shrug Form",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Explosive barbell shrug with hip drive to develop power.",
        "Hold a barbell with an overhand grip in front of your thighs. Hinge slightly at the hips with a flat back. Explosively extend your hips and knees while shrugging the weight upward. Land softly and reset your stance. Repeat with full power on each rep.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS)
    ),
    Exercise(
        455,
        "Trap Bar Jump Squat",
        MuscleGroup.QUADS,
        Equipment.TRAP_BAR,
        "Explosive lower-body power movement using a hex bar.",
        "Stand inside the trap bar and grip the handles. Lower into a squat with a flat back. Explosively jump upward through your legs. Land softly with your knees bent and controlled. Reset and repeat each rep with full effort.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES)
    ),
    Exercise(
        456,
        "Trap Bar Bent Over Row",
        MuscleGroup.BACK,
        Equipment.TRAP_BAR,
        "Bent-over row using a hex bar with a neutral grip for the mid-back.",
        "Stand inside the trap bar and hinge at the hips with a flat back. Grip the handles with a neutral grip. Pull the bar toward your lower chest. Squeeze your shoulder blades together at the top. Lower under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        457,
        "Trap Bar Farmers Carry",
        MuscleGroup.FOREARMS,
        Equipment.TRAP_BAR,
        "Heavy loaded carry with a trap bar to build grip and core stability.",
        "Stand inside the trap bar and grip the handles. Deadlift the bar to a standing position. Brace your core and keep your shoulders back. Take short controlled steps forward. Set the bar down safely at the end.",
        secondaryMuscles = listOf(MuscleGroup.TRAPS, MuscleGroup.ABS, MuscleGroup.FULL_BODY)
    ),
    Exercise(
        458,
        "EZ Barbell Seated Triceps Extension",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Seated overhead extension using an EZ-bar to isolate the triceps.",
        "Sit upright holding the EZ-bar overhead with both hands. Keep your elbows tucked close to your head. Lower the weight behind your head by bending your elbows. Extend your arms back to full lockout. Squeeze your triceps at the top."
    ),
    Exercise(
        459,
        "EZ Barbell Standing Wrist Reverse Curl",
        MuscleGroup.FOREARMS,
        Equipment.EZ_BAR,
        "Standing forearm extension using an overhand reverse grip.",
        "Hold the EZ-bar with an overhand grip in front of your thighs. Keep your arms still and elbows fixed. Extend your wrists upward to lift the weight. Pause at the top of the movement. Lower the bar slowly to the start."
    ),
    Exercise(
        460,
        "EZ Barbell Anti Gravity Press",
        MuscleGroup.SHOULDERS,
        Equipment.EZ_BAR,
        "Chest-supported overhead press performed lying face-down on an incline bench.",
        "Lie chest-down on an incline bench. Hold the EZ-bar at shoulder level beneath you. Press it straight upward against gravity. Squeeze your shoulders at the top. Lower under control to the start.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.TRAPS)
    ),
    Exercise(
        461,
        "EZ Bar Kneeling Rollout",
        MuscleGroup.ABS,
        Equipment.EZ_BAR,
        "Core rollout using a loaded EZ-bar as a wheel.",
        "Kneel on the floor gripping the EZ-bar with both hands. Brace your core and keep a flat back. Roll the bar forward as far as you can control. Pull the bar back toward your knees using your abs. Return to the upright start position.",
        secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        462,
        "EZ Barbell Decline Close Grip Face Press",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Decline close-grip press lowering toward the face to target the triceps.",
        "Lie on a decline bench with your feet secured. Hold the EZ-bar with a close grip over your chest. Lower it toward your forehead by bending your elbows. Press the bar back up to lockout. Squeeze your triceps at the top."
    ),
    Exercise(
        463,
        "EZ Barbell Standing Wrist Curl",
        MuscleGroup.FOREARMS,
        Equipment.EZ_BAR,
        "Standing wrist curl with an EZ-bar to build forearm flexors.",
        "Hold the EZ-bar with an underhand grip in front of you. Keep your arms still and elbows locked. Flex your wrists upward toward your body. Pause at the top of the curl. Lower the bar slowly to a full stretch."
    ),
    Exercise(
        464,
        "EZ Bar Incline Front Raise",
        MuscleGroup.SHOULDERS,
        Equipment.EZ_BAR,
        "Incline chest-supported front raise targeting the front delts.",
        "Lie chest-down on an incline bench holding the EZ-bar. Let your arms hang straight down. Raise your arms forward until they are parallel to the floor. Pause briefly at the top. Lower under control to the start."
    ),
    Exercise(
        465,
        "EZ Bar Seated Wrist Reverse Curl",
        MuscleGroup.FOREARMS,
        Equipment.EZ_BAR,
        "Seated wrist extension using an EZ-bar to isolate the forearm extensors.",
        "Sit on a bench with your forearms resting on your thighs. Hold the bar with an overhand grip and wrists hanging off your knees. Extend your wrists upward to lift the weight. Pause at the top. Lower the bar slowly to a stretch."
    ),
    Exercise(
        466,
        "EZ Barbell Spider Curl",
        MuscleGroup.BICEPS,
        Equipment.EZ_BAR,
        "Chest-supported biceps curl on an incline bench to prevent momentum.",
        "Lie chest-down on an incline bench. Let your arms hang straight down holding the EZ-bar. Curl the weight upward toward your shoulders. Squeeze your biceps at the top. Lower under control to a full stretch."
    ),
    Exercise(
        467,
        "EZ Bar Seated Close Grip Shoulder Press",
        MuscleGroup.SHOULDERS,
        Equipment.EZ_BAR,
        "Seated overhead press with a narrow grip on an EZ-bar.",
        "Sit upright on a bench with your back supported. Hold the EZ-bar at shoulder height with a narrow grip. Press it straight up overhead to lockout. Pause briefly at the top. Lower the bar under control to your shoulders.",
        secondaryMuscles = listOf(MuscleGroup.TRICEPS)
    ),
    Exercise(
        468,
        "EZ Barbell Incline Triceps Extension",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Incline skull crusher giving a deeper stretch on the triceps long head.",
        "Lie on an incline bench holding the EZ-bar overhead. Keep your upper arms angled back slightly. Bend your elbows to lower the bar past your head. Extend your arms back to lockout. Squeeze your triceps at the top."
    ),
    Exercise(
        469,
        "EZ Bar Reverse Grip Bent Over Row",
        MuscleGroup.BACK,
        Equipment.EZ_BAR,
        "Bent-over row using an underhand grip on an EZ-bar.",
        "Hinge forward at the hips with a flat back. Hold the EZ-bar with an underhand grip. Pull it toward your lower abdomen. Squeeze your shoulder blades together at the top. Lower under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        470,
        "EZ Bar Seated Close Grip Concentration Curl",
        MuscleGroup.BICEPS,
        Equipment.EZ_BAR,
        "Seated concentration curl using both hands on an EZ-bar to isolate the biceps.",
        "Sit on a bench and lean forward slightly. Rest your elbows against the insides of your thighs. Hold the EZ-bar with a close underhand grip. Curl the bar upward toward your shoulders. Lower slowly to a full stretch."
    ),
    Exercise(
        471,
        "EZ Bar Standing French Press",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Standing overhead triceps extension using an EZ-bar.",
        "Stand upright holding the EZ-bar overhead with both hands. Keep your elbows tucked close to your head. Lower the weight behind your head by bending your elbows. Extend your arms back to full lockout. Squeeze your triceps at the top."
    ),
    Exercise(
        472,
        "EZ Bar Close Grip Bench Press",
        MuscleGroup.TRICEPS,
        Equipment.EZ_BAR,
        "Close-grip bench press using an EZ-bar to reduce wrist strain.",
        "Lie flat on a bench with your feet planted. Hold the EZ-bar with a narrow grip over your chest. Lower it under control to your sternum. Press the bar back up to lockout. Keep your elbows tucked throughout.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        473,
        "Smith Machine Incline Tricep Extension",
        MuscleGroup.TRICEPS,
        Equipment.SMITH_MACHINE,
        "Incline skull crusher along a fixed track for strict triceps isolation.",
        "Set an incline bench inside the Smith machine. Lie back and grip the bar with a close grip. Lower the bar smoothly toward your forehead. Extend your elbows to press the bar back up. Squeeze your triceps at the top."
    ),
    Exercise(
        474,
        "Smith Machine Bicep Curl",
        MuscleGroup.BICEPS,
        Equipment.SMITH_MACHINE,
        "Strict biceps curl using the guided Smith machine bar to eliminate swinging.",
        "Stand facing the bar and grip it with an underhand grip at shoulder width. Keep your elbows tucked close to your sides. Curl the bar upward along the fixed vertical path. Squeeze your biceps at the top, then lower under control."
    ),
    Exercise(
        475,
        "Cable Hip Adduction",
        MuscleGroup.GLUTES,
        Equipment.CABLE,
        "Cable isolation exercise targeting the inner thigh adductor muscles.",
        "Attach an ankle cuff to the low pulley and stand sideways to the machine. Brace your core and keep your standing leg stable. Sweep your working leg across the front of your body against the resistance. Return slowly to the starting position."
    ),
    Exercise(
        476,
        "Cable Hip Abduction",
        MuscleGroup.GLUTES,
        Equipment.CABLE,
        "Cable isolation exercise targeting the outer glutes and hip abductors.",
        "Attach an ankle cuff to the low pulley and stand sideways to the machine. Keep your torso upright and core braced. Sweep your outer leg away from your body against the resistance. Lower it back slowly under control."
    ),
    Exercise(
        477,
        "Barbell Lying Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.BARBELL,
        "Lying preacher curl variation providing unique tension angles to isolate the biceps.",
        "Lie prone on a bench with your upper arms supported against a pad. Hold a barbell with an underhand grip. Curl the bar upward toward your face. Lower it slowly until your arms are nearly straight."
    ),
    Exercise(
        478,
        "Dumbbell Standing Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Standing preacher curl using dumbbells for isolated arm development.",
        "Stand behind a preacher bench and rest your upper arms on the pad. Hold a dumbbell in each hand with an underhand grip. Curl the dumbbells upward toward your shoulders. Lower them under control to a full stretch."
    ),
    Exercise(
        479,
        "Dumbbell Preacher Hammer Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Preacher curl performed with a neutral grip to target the brachialis and brachioradialis.",
        "Rest your arms on a preacher pad with dumbbells held in a neutral grip. Keep your palms facing each other throughout. Curl the dumbbells toward your shoulders. Lower them slowly back to the start."
    ),
    Exercise(
        480,
        "Barbell Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.BARBELL,
        "Classic barbell preacher curl for strict biceps isolation by removing momentum.",
        "Sit at a preacher bench and rest your upper arms fully on the pad. Hold a barbell with an underhand grip. Curl the bar upward without lifting your elbows off the pad. Lower it slowly to a near-full extension."
    ),
    Exercise(
        481,
        "Dumbbell One Arm Zottman Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.DUMBBELL,
        "Unilateral preacher curl using a Zottman rotation to target the biceps and forearms.",
        "Rest one arm on the preacher pad and hold a dumbbell with an underhand grip. Curl the dumbbell up toward your shoulder. Rotate your wrist to an overhand grip at the top. Lower the weight slowly back down."
    ),
    Exercise(
        482,
        "Cable Kneeling Preacher Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Kneeling preacher curl variation using cable tension for constant resistance.",
        "Kneel facing a cable machine and rest your elbows on a bench pad. Hold the cable bar with an underhand grip. Curl the bar toward your forehead. Lower it slowly while keeping tension on the cable."
    ),
    Exercise(
        483,
        "Landmine 180",
        MuscleGroup.ABS,
        Equipment.BARBELL,
        "Rotational core movement tracing a wide arc with a landmine barbell.",
        "Stand facing the landmine and hold the end of the sleeve with both hands. Extend your arms out in front of you and brace your core. Rotate the bar from one hip to the other in a smooth arc. Control the movement and keep your hips engaged.",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        484,
        "Landmine Front Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Squat variation holding the landmine sleeve at chest level to emphasize the quads.",
        "Stand facing the bar and cup the sleeve in both hands against your chest. Keep your chest up and core braced. Lower into a deep squat against the angled resistance. Drive through your heels to stand back up.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        485,
        "Landmine Lateral Raise",
        MuscleGroup.SHOULDERS,
        Equipment.BARBELL,
        "Unilateral shoulder isolation tracing the natural arc of a landmine setup.",
        "Stand sideways to the landmine bar and grip the end of the sleeve with your outer hand. Keep a slight bend in your elbow. Raise your arm out to the side along the bar's arc. Lower it slowly back down."
    ),
    Exercise(
        486,
        "Landmine Sumo Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Wide-stance squat using a landmine barbell to target the quads, adductors and glutes.",
        "Assume a wide stance facing the bar and hold the sleeve down between your legs. Point your toes slightly outward. Lower your hips down while keeping your knees pushed out. Drive back up through your heels.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        487,
        "Landmine One Arm Bent Over Bench Row",
        MuscleGroup.BACK,
        Equipment.BARBELL,
        "Single-arm landmine row with a bench providing chest support to spare the lower back.",
        "Lie chest-down on an incline bench positioned next to the landmine. Grip the sleeve with one hand and let your arm hang. Pull the bar upward toward your waist while squeezing your shoulder blade. Lower it under control to a full stretch.",
        secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        488,
        "Landmine Stand Up High Knee",
        MuscleGroup.ABS,
        Equipment.BARBELL,
        "Dynamic core and hip flexor movement driving the knee up against landmine resistance.",
        "Stand facing the bar and hold the sleeve at your shoulder with one hand. Step back into a slight stagger and brace your core. Drive that same-side knee forward and upward into a high knee. Return the leg under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        489,
        "Landmine Rear Lunge",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Reverse lunge holding the landmine bar for a fixed, stable path of travel.",
        "Stand facing the barbell and hold the sleeve at your chest. Step backward with one leg into a lunge until your back knee nears the floor. Keep your torso upright and core braced. Push back through your front heel to the start.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        490,
        "Landmine Hack Squat",
        MuscleGroup.QUADS,
        Equipment.BARBELL,
        "Squat variation leaning back against the landmine bar to emphasize the quads.",
        "Stand facing away from the landmine and rest the end of the sleeve on one shoulder. Lean slightly back into the bar with a braced core. Squat down until your thighs are parallel. Drive through your heels to stand tall.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        491,
        "Landmine Romanian Deadlift",
        MuscleGroup.HAMSTRINGS,
        Equipment.BARBELL,
        "Hip hinge using a landmine barbell to target the hamstrings and glutes.",
        "Stand facing the bar and hold the sleeve with both hands. Keep a flat back and soft knees. Hinge forward at your hips until you feel a stretch in your hamstrings. Squeeze your glutes to drive back upright.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        492,
        "Landmine Single Leg Hip Thrust",
        MuscleGroup.GLUTES,
        Equipment.BARBELL,
        "Unilateral hip extension using a landmine barbell positioned across one hip.",
        "Sit on the floor with your upper back against a bench. Rest the landmine sleeve across one hip and plant that foot flat. Elevate the other leg and drive your hips upward through your heel. Lower under control and repeat.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        493,
        "Landmine Single Leg Romanian Deadlift",
        MuscleGroup.HAMSTRINGS,
        Equipment.BARBELL,
        "Unilateral hip hinge using the landmine arc to build balance and hamstring strength.",
        "Stand facing the bar and hold the sleeve in one hand. Balance on one leg with a soft knee. Hinge at your hips while extending the non-working leg straight behind you. Return to standing by squeezing your glute.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)
    ),
    Exercise(
        494,
        "Cable Seated Leg Extension",
        MuscleGroup.QUADS,
        Equipment.CABLE,
        "Seated leg extension using a low cable pulley to isolate the quads.",
        "Sit on a bench facing away from the pulley with an ankle cuff attached. Secure yourself and grip the bench for stability. Extend your knee forward until your leg is straight. Lower slowly back to the bent position."
    ),
    Exercise(
        495,
        "One Arm Cable Curl",
        MuscleGroup.BICEPS,
        Equipment.CABLE,
        "Single-arm cable curl providing constant tension to isolate the biceps.",
        "Stand facing the low pulley and hold the single handle with an underhand grip. Keep your elbow tucked close to your side. Curl the handle upward toward your shoulder. Lower it slowly while maintaining cable tension."
    ),
    Exercise(
        496,
        "Elevated Seated Calf Raise",
        MuscleGroup.CALVES,
        Equipment.DUMBBELL,
        "Seated calf raise performed on an elevated platform for increased range of motion.",
        "Sit with your thighs secured under the pads and the balls of your feet on the platform. Lower your heels down for a full stretch. Press upward through your toes as high as possible. Pause at the top, then lower under control."
    ),
    Exercise(
        497,
        "Smith Machine One Leg Floor Calf Raise",
        MuscleGroup.CALVES,
        Equipment.SMITH_MACHINE,
        "Unilateral calf raise performed on the floor using a Smith machine for balance.",
        "Place the Smith machine bar across your shoulders and balance on one foot. Keep your standing leg slightly bent for stability. Lift your heel as high as possible to contract the calf. Lower it slowly back to the floor."
    ),
    Exercise(
        498,
        "Seated Calf Raise on Leg Press Machine",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Calf extension performed while seated at a leg press machine.",
        "Sit in the machine and place the balls of your toes on the bottom edge of the sled. Release the safety and keep your legs nearly straight. Flex your ankles to push the sled away. Lower your heels back for a deep stretch."
    ),
    Exercise(
        499,
        "Smith Machine Seated Calf Raise",
        MuscleGroup.CALVES,
        Equipment.SMITH_MACHINE,
        "Seated calf raise using a Smith machine bar for resistance.",
        "Sit on a bench with your toes on a block and the Smith bar resting across your lower thighs with a pad. Release the bar from the hooks. Raise your heels upward by flexing your ankles. Lower them slowly for a full stretch."
    ),
    Exercise(
        500,
        "Cable Standing Calf Raise",
        MuscleGroup.CALVES,
        Equipment.CABLE,
        "Standing calf raise using a low cable pulley to maintain constant tension.",
        "Stand facing away from a low pulley wearing a waist belt or holding handles. Place the balls of your feet on a block. Raise your heels as high as possible against the resistance. Lower them slowly for a deep stretch."
    ),
    Exercise(
        501,
        "Seated Calf Press on Leg Press Machine",
        MuscleGroup.CALVES,
        Equipment.MACHINE,
        "Strict calf press driving the sled with the ankles along a horizontal path.",
        "Sit back into the pad and extend your legs fully. Position your toes on the lower edge of the plate. Push the sled away by extending your ankles. Lower your heels back slowly to stretch the calves."
    ),
    Exercise(
        502,
        "Smith Machine Leg Press",
        MuscleGroup.QUADS,
        Equipment.SMITH_MACHINE,
        "Vertical leg press alternative pressing up against a Smith machine barbell.",
        "Lie flat on your back beneath the bar and place your feet securely on the underside of the barbell. Unlock the bar from the safety hooks. Lower the weight by bending your knees toward your chest. Press upward until your legs are nearly straight.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        503,
        "Full Crunch Machine",
        MuscleGroup.ABS,
        Equipment.MACHINE,
        "Machine crunch that simultaneously moves the upper torso and the hips.",
        "Sit in the machine and secure your feet while grasping the handles. Brace your abs to begin. Crunch your chest and knees toward each other. Return slowly to the stretched starting position."
    ),
    Exercise(
        504,
        "Seated Twist Machine",
        MuscleGroup.ABS,
        Equipment.MACHINE,
        "Rotary machine designed to isolate the internal and external oblique muscles.",
        "Sit upright with your posture tall and grip the handles or rest against the upper pad. Keep your hips facing forward. Rotate your torso to one side against the resistance. Control the return and repeat to the other side."
    ),
    Exercise(
        505,
        "Seated Crunch Machine",
        MuscleGroup.ABS,
        Equipment.MACHINE,
        "Seated core machine focusing on upper abdominal flexion.",
        "Sit comfortably back against the pad and hold the overhead grips. Keep your hips stable. Flex your torso forward by contracting your abs. Return slowly to the upright position under control."
    ),
    Exercise(
        506,
        "Seated Ab Crunch Machine",
        MuscleGroup.ABS,
        Equipment.MACHINE,
        "Seated isolation machine providing controlled resistance for the rectus abdominis.",
        "Sit in the machine and rest your arms or chest against the padded levers. Brace your core before moving. Contract your abs to push the load forward. Return slowly to the start without letting the weight pull you back."
    ),
    Exercise(
        507,
        "Barbell Lying Triceps Extension Skull Crusher",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Classic barbell skull crusher performed on a flat bench to isolate the triceps.",
        "Lie on a flat bench holding the barbell over your chest with a narrow grip. Keep your upper arms vertical and elbows fixed. Bend your elbows to lower the bar toward your forehead. Extend your arms to press the bar back up."
    ),
    Exercise(
        508,
        "Dumbbell Lying Floor Skullcrusher",
        MuscleGroup.TRICEPS,
        Equipment.DUMBBELL,
        "Floor-based skull crusher using dumbbells to limit momentum and protect the elbows.",
        "Lie flat on the floor holding a dumbbell in each hand with a neutral grip. Position your arms vertically over your shoulders. Lower the dumbbells down beside your ears by bending your elbows. Extend your arms to press them back up."
    ),
    Exercise(
        509,
        "Barbell Incline Triceps Extension Skull Crusher",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Incline bench skull crusher that increases the stretch on the long head of the triceps.",
        "Lie on an incline bench holding the barbell overhead with a narrow grip. Keep your upper arms angled slightly back. Lower the bar past the top of your head by bending your elbows. Press the bar back up to full extension."
    ),
    Exercise(
        510,
        "Kettlebell Lying Triceps Extension Skull Crusher",
        MuscleGroup.TRICEPS,
        Equipment.KETTLEBELL,
        "Flat bench skull crusher using kettlebells to alter the resistance profile.",
        "Lie on a bench holding a kettlebell in each hand by the handles. Position your arms vertically over your chest. Lower the bells under control toward your shoulders. Extend your arms to press them back overhead."
    ),
    Exercise(
        511,
        "Barbell Decline Close Grip to Skull Press",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Decline movement blending a close-grip press with a skull crusher for the triceps.",
        "Lie on a decline bench holding the barbell with a close grip. Lower the bar toward your forehead by bending your elbows. Then press it forward and up in a close-grip path over your chest. Repeat the combined motion under control.",
        secondaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
    ),
    Exercise(
        512,
        "Cable Incline Skull Crusher",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Incline bench skull crusher using a cable to maintain constant tension through the arc.",
        "Set an incline bench facing away from a low cable pulley. Hold the bar attachment overhead with your upper arms fixed. Lower it behind your head by bending your elbows. Extend your arms to press the bar back up."
    ),
    Exercise(
        513,
        "Barbell Reverse Grip Skullcrusher",
        MuscleGroup.TRICEPS,
        Equipment.BARBELL,
        "Skull crusher performed with an underhand grip to shift stress toward the medial head.",
        "Lie on a flat bench holding the barbell with a reverse underhand grip. Keep your upper arms vertical and elbows fixed. Lower the bar toward your forehead under control. Extend your arms to press the bar back up."
    ),
    Exercise(
        514,
        "Band Skull Crusher",
        MuscleGroup.TRICEPS,
        Equipment.RESISTANCE_BAND,
        "Triceps isolation exercise using an elastic band for progressive resistance.",
        "Anchor a band behind you and hold the ends while lying flat or standing. Position your upper arms fixed and elbows pointing forward. Bend your elbows to lower your hands toward your head. Extend your arms fully against the band's resistance."
    ),
    Exercise(
        515,
        "Seated Leg Press Machine",
        MuscleGroup.QUADS,
        Equipment.MACHINE,
        "A compound lower body exercise that isolates the legs while supporting the lower back.",
        "Sit firmly in the machine with your back flat against the pad. Place your feet shoulder-width apart on the sled. Release the safety handles and lower the weight under control until your knees are at a 90-degree angle. Push the platform back up using your entire foot, ensuring you do not lock out your knees.",
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
    ),
    Exercise(
        516,
        "Ticep Cable Kickback",
        MuscleGroup.TRICEPS,
        Equipment.CABLE,
        "Cable kickback tricep isolation.",
        "Set the cable pulley to a low or mid-low position and attach a single D-handle. Grasp the handle with one hand and step back from the machine. Hinge forward at the hips until your torso is roughly parallel to the floor, supporting yourself with your free hand on your knee or the machine. Raise your upper arm so it is parallel to the floor and pin it in place, with your elbow bent at 90 degrees. Extend your forearm backward by straightening your elbow, squeezing the tricep hard at full extension. Hold the fully extended position for a brief pause. Slowly bend your elbow to return to the starting position without dropping your upper arm. Complete all repetitions on one arm before switching to the other side."
    ),
    Exercise(
        517,
        "Standing Glute Kickback",
        MuscleGroup.GLUTES,
        Equipment.MACHINE,
        "Cable kickback tricep isolation.",
        "Adjust the machine to your body. The knees should be facing forward and resting near the bottom of the padding. Place your forearms on the upper padding and grip the handles. Next, make sure that you have the arm of the machine under your knee. Brace your core, keep your body still, and start to press your leg backwards and upwards by activating the glute. Push the leg as far up as you can without rotating in the hips, then return to the starting position. Do all reps on one side first, and then switch to the other leg.",
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS)
    )
)

val exerciseSeedData: List<Exercise> = rawExerciseSeedData.map {
    it.copy(classification = classifyExercise(it.name, it.equipment, it.muscleGroup))
}
