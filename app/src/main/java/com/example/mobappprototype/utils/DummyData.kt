package com.example.mobappprototype.utils

import com.example.mobappprototype.R
import com.example.mobappprototype.model.BioDescription
import com.example.mobappprototype.model.Days
import com.example.mobappprototype.model.Review
import com.example.mobappprototype.model.Schedule
import com.example.mobappprototype.model.Subject
import com.example.mobappprototype.model.Tutor
import com.example.mobappprototype.model.TutorProfile
import com.example.mobappprototype.model.UserComments

object DummyData {
    val subjectList = listOf(
        Subject.GenMath,
        Subject.Physics,
        Subject.Chemistry,
        Subject.Biology,
        Subject.ComputerScience,
        Subject.Law,
        Subject.Architecture,
        Subject.Business,
        Subject.IT,
        Subject.Engineering,
        Subject.MachineLearning,
        Subject.Calculus
    )
    val bioDescriptions = listOf(
        BioDescription("Do people even read this section? Are you real? We are in a simulation! Wake up!"),
        BioDescription("I believe that JJK is overrated af. Fight me!"),
        BioDescription("Surviving cancer patient. If that does not guilt trip you to hire me, then you support Hitler!"),
        BioDescription("A violent tutor who will kidnap you and take you to a BDSM chamber if you make a mistake! I want students to enjoy punishments, " +
                "and make them want to make mistakes more instead. Let's get dumb together!"),
        BioDescription("Hire me please, I'm broke and my wife left me."),
        BioDescription("Creative and innovative teacher, just like my hero Hitler! Go Palestine! Kill those Jews and make father proud!"),
        BioDescription("Subject matter expert with a deep understanding of how to manipulate all my students to give me more money. Don't believe me? Try me!"),
        BioDescription("I make sure that students learn by implementing my torture lesson technique. " +
                "If they have 1 mistake, I punch them 10x. This method might be overly brutal to some of you but I for one can assure you that the results speak louder than words or something."),
        BioDescription("If you are looking for the best Tutor for your needs, then look elsewhere. I am not what you are looking for!"),
        BioDescription("I don't do tutoring, I just went here to advertise my newest product! The Extreme HeroBrine 3000xx Super El Machina Vibratron!"),
        BioDescription("I swear, If students use my class as their e-dating spot again, I'm gonna kms!"),
        BioDescription("You can use me as the reason why you are late at home, I will even call them. I don't ask any questions why, I just do my job."),
        BioDescription("Interested in buying illegal drugs? Want to hire a hitman? Want me to kidnap your bitch wife? Say no more brother, I'm your private Tutor!"),
        BioDescription("Are you lonely? are your sad? did your girl left you? Are you tired of your pathetic life? " +
                "Then don't hire me, dumbass. Why are you even here if that's the case? Just kys and make the world a better place."),
        BioDescription("A safe place for dumb people. We can all talk about why you are so dumb here, safely. Don't be shy, let use know why you are so dumb. " +
                "It needs to be studied, seriously."),
        BioDescription("I just want a place to yell about students and feel good about myself even though I am a pathetic loser without any accomplishments and is still single af. " +
                "Tutoring is great, honestly.")
        )


    val userComments = listOf(
        UserComments("Great tutor! His hands-on tutoring style helped me elevate to newer heights!"),
        UserComments("Great at subject, bad at Tutoring. You are better off just reading than to listen to this clown!"),
        UserComments("Made me cheat just to pass my exams! Never Again!"),
        UserComments("Entertaining clown! It was fun seeing him try to tutor and fail spectacularly."),
        UserComments("A great tutor who goes the extra mile to for his students! " +
                "I even saw him on my bedroom that one time just to prepare for our tutor sessions ahead of time! Astonishing!"),
        UserComments("I couldn't be happier with the tutoring I received. Yes, I was never happier than normal with this tutor. Boring!"),
        UserComments("The tutor was knowledgeable, but could have been more engaging. It's all just a yap sessions for hours upon end."),
        UserComments("Beware, Indian Accent! Save your money and go elsewhere."),
        UserComments("Lazy Tutor! Just tells you to read and lectures you if you want to ask questions"),
        UserComments("Hot Tutor! I recommend requesting a home tutor from her too!"),
        UserComments("I was disappointed with the quality of tutoring I received. She keeps on seducing me, instead of teaching me!"),
        UserComments("I would not recommend this tutor to anyone, not even my worst enemies"),
        UserComments("If you want to further be confused, hire this clown disguised as a pathetic excuse of a so-called Tutor."),
        UserComments("Do you want to experience having a parent that lectures and judges you all day everyday? Then this tutor is for you!"),
        UserComments("This Tutor hallucinates a lot! Needs more updates."),
        UserComments("Waste of Time and Money! Please Fire this Tutor"),
        UserComments("Underrated tutor, Don't be mean guys, this tutor at least deserves 2/5, not 1/5."),
        UserComments("Does this tutor even know what it's teaching? This mf just reads powerpoints and give assignments!"),
        UserComments("Boring Tutor! Does not even accept other method of payments besides cash!"),
        UserComments("All of his tutoring could have been an email!"),
        UserComments("Made me a better person by realizing that I am better than him!"),
        UserComments("I am writing this before commiting suicide, but it is all this tutor's fault!"),
        UserComments("I learnt way more watching a wall than watching this tutor for many hours."),
        UserComments("There are a lot of ways to torture a person but getting this guy to be your tutor is certainly high up on the list!")
    )

    val tutorList = mutableListOf(
        Tutor("Michael Jackson", R.drawable.james, listOf(Subject.Engineering, Subject.ComputerScience), 4.2f),
        Tutor("Alice Jones", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.7f),
        Tutor("David Williams", R.drawable.james, listOf(Subject.Physics, Subject.GenMath), 3.8f),
        Tutor("Emily White", R.drawable.james, listOf(Subject.Calculus, Subject.Biology), 4.9f),
        Tutor("Charles Davis", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.3f),
        Tutor("Sarah Connor", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 4.1f),
        Tutor("Disgusting Clark", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.6f),
        Tutor("Zinnia What", R.drawable.james, listOf(Subject.Calculus, Subject.Chemistry), 4.8f),
        Tutor("Richard Hernandez", R.drawable.james, listOf(Subject.GenMath, Subject.Physics), 3.9f),
        Tutor("Iam Young", R.drawable.james, listOf(Subject.IT, Subject.MachineLearning), 4.4f),
        Tutor("Micheal James", R.drawable.james, listOf(Subject.Engineering, Subject.Architecture), 4.0f),
        Tutor("Glizzy Hands", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.5f),
        Tutor("Christopher Colombus", R.drawable.james, listOf(Subject.Calculus, Subject.GenMath), 3.7f),
        Tutor("Ashley Lewis", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 5.0f),
        Tutor("Matthew Garcia", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.2f),
        Tutor("Nicole Beatsu", R.drawable.james, listOf(Subject.IT, Subject.Calculus), 4.8f),
        Tutor("Joseph Christ", R.drawable.james, listOf(Subject.Business, Subject.Law), 3.5f),
        Tutor("Catherine Allen", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 4.9f),
        Tutor("Robert Thompson", R.drawable.james, listOf(Subject.GenMath, Subject.Physics), 4.1f),
        Tutor("Jessica Sanchez", R.drawable.james, listOf(Subject.IT, Subject.MachineLearning), 4.6f),
        Tutor("Andrew Walker", R.drawable.james, listOf(Subject.Engineering, Subject.Architecture), 4.4f),
        Tutor("Margaret Wilson", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.7f),
        Tutor("Kevin Lost", R.drawable.james, listOf(Subject.Physics, Subject.GenMath), 3.9f),
        Tutor("Kimberly Moore", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 4.5f),
        Tutor("Ronald Chrisistimo", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.0f),
        Tutor("Sandra Child", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 4.2f),
        Tutor("Damn Daniel", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.6f),
        Tutor("Elizabeth Moore", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 4.8f),
        Tutor("Die Taytay", R.drawable.james, listOf(Subject.Calculus, Subject.Physics), 4.3f),
        Tutor("Makima Beloved", R.drawable.james, listOf(Subject.IT, Subject.MachineLearning), 3.2f),
        Tutor("Michael Lee", R.drawable.james, listOf(Subject.Engineering, Subject.ComputerScience), 4.2f),
        Tutor("White Jones", R.drawable.james, listOf(Subject.Calculus, Subject.Business), 4.7f),
        Tutor("Umbrella Forgotten", R.drawable.james, listOf(Subject.Physics, Subject.Calculus), 3.8f),
        Tutor("Emily Brown", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 4.9f),
        Tutor("Charles Davis", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.3f),
        Tutor("Sarah Miller", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 4.1f),
        Tutor("Will Clark", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.6f),
        Tutor("Jennifer Garcia", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 4.8f),
        Tutor("Richard Gomey", R.drawable.james, listOf(Subject.GenMath, Subject.Physics), 3.9f),
        Tutor("Amanda Old", R.drawable.james, listOf(Subject.IT, Subject.MachineLearning), 4.4f),
        Tutor("Dwayne Johnson", R.drawable.james, listOf(Subject.Engineering, Subject.Architecture), 4.0f),
        Tutor("Gojo Dead", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.5f),
        Tutor("Arthur Morgan", R.drawable.james, listOf(Subject.Calculus, Subject.GenMath), 3.7f),
        Tutor("Ashley Lewis", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 5.0f),
        Tutor("Father Jesus", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.2f),
        Tutor("Nicole Baker", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 4.8f),
        Tutor("Very Good", R.drawable.james, listOf(Subject.Business, Subject.Law), 3.5f),
        Tutor("Catherine Martinez", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 5.0f),
        Tutor("Robert Thompson", R.drawable.james, listOf(Subject.GenMath, Subject.Physics), 4.1f),
        Tutor("Jessica Sanchez", R.drawable.james, listOf(Subject.IT, Subject.MachineLearning), 4.6f),
        Tutor("Andrew Walker", R.drawable.james, listOf(Subject.Calculus, Subject.Architecture), 4.4f),
        Tutor("Margaretto Wilson", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.7f),
        Tutor("Kevin Gone", R.drawable.james, listOf(Subject.Physics, Subject.GenMath), 3.9f),
        Tutor("Kimberly Less", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 4.5f),
        Tutor("Ronaldo NotbetterthanMessi", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.0f),
        Tutor("Sandra Young", R.drawable.james, listOf(Subject.Calculus, Subject.ComputerScience), 4.2f),
        Tutor("Daniel Johnson", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.6f),
        Tutor("Elizabeth Moore", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 4.8f),
        Tutor("Christopher Taylor", R.drawable.james, listOf(Subject.GenMath, Subject.Physics), 4.3f),
        Tutor("Pron Lewis", R.drawable.james, listOf(Subject.IT, Subject.Calculus), 3.2f),
        Tutor("Ethan Davis", R.drawable.james, listOf(Subject.ComputerScience, Subject.GenMath), 4.7f),
        Tutor("Water Bottle", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.2f),
        Tutor("Noah lyles", R.drawable.james, listOf(Subject.Physics, Subject.Chemistry), 3.8f),
        Tutor("Ava Perez", R.drawable.james, listOf(Subject.Biology, Subject.IT), 4.9f),
        Tutor("Jacob Twah", R.drawable.james, listOf(Subject.Engineering, Subject.Architecture), 4.3f),
        Tutor("Sophia Martinez", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.1f),
        Tutor("William Garcia", R.drawable.james, listOf(Subject.GenMath, Subject.Calculus), 4.6f),
        Tutor("XxXKillahRahhhXxX", R.drawable.james, listOf(Subject.Chemistry, Subject.Biology), 4.8f),
        Tutor("Charles HatesJimmy", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 3.9f),
        Tutor("Sarah Martinez", R.drawable.james, listOf(Subject.Business, Subject.Law), 4.4f),
        Tutor("Daniel Garcia", R.drawable.james, listOf(Subject.Engineering, Subject.Architecture), 4.0f),
        Tutor("Quality Name", R.drawable.james, listOf(Subject.Law, Subject.Business), 4.5f),
        Tutor("Johnny Sinner", R.drawable.james, listOf(Subject.Physics, Subject.GenMath), 3.7f),
        Tutor("Trashley Hopez", R.drawable.james, listOf(Subject.Calculus, Subject.Biology), 5.0f),
        Tutor("Matthew Garcia", R.drawable.james, listOf(Subject.Architecture, Subject.Engineering), 4.2f),
        Tutor("Nicker Rodriguez", R.drawable.james, listOf(Subject.IT, Subject.ComputerScience), 4.8f),
        Tutor("Jeff Mynameis", R.drawable.james, listOf(Subject.Business, Subject.Law), 3.5f),
        Tutor("Jonathan Martinez", R.drawable.james, listOf(Subject.Biology, Subject.Chemistry), 5.0f)
    )

    fun generateSchedule(): List<Schedule> {
        val days = Days.entries.filter { Math.random() < 0.5 }.toList() // Randomly select 5 days
        val schedule = mutableListOf<Schedule>()
        for (day in days) {
            val startTime = TimeUtils.timesList.random()
            var endTime = TimeUtils.timesList.random()
            while (!TimeUtils.isEarlier(startTime, endTime)) {
                endTime = TimeUtils.timesList.random()
            }
            schedule.add(Schedule(day.name, startTime, endTime))
        }
        return schedule
    }

    fun generateReviews(numReviews: Int): List<Review> {
        val reviews = mutableListOf<Review>()
        for (i in 0 until numReviews) {
            reviews.add(
                Review(
                    "User $i",
                    Math.random().toFloat() * 5, // Random rating between 0 and 5
                    comment = userComments.random().toString()
                )
            )
        }
        return reviews
    }

    fun generateTutorProfile(tutor: Tutor): TutorProfile {
        val numReviews = (5..10).random() // Generate random number of reviews between 1 and 5
        return TutorProfile(
            tutor = tutor,
            bioDescription = bioDescriptions.random().toString(),
            schedules = generateSchedule(),
            reviews = generateReviews(numReviews)
        )
    }

}