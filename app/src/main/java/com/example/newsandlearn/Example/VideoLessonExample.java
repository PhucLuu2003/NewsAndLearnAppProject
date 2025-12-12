package com.example.newsandlearn.Example;

import android.content.Intent;
import com.example.newsandlearn.Activity.VideoLessonActivity;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.VideoLesson;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

/**
 * Example code showing how to create and use Video Lessons
 * This class demonstrates:
 * 1. How to create VideoLesson objects
 * 2. How to add them to Firestore
 * 3. How to launch VideoLessonActivity
 */
public class VideoLessonExample {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Example 1: Create a video lesson with multiple choice questions
     */
    public VideoLesson createMultipleChoiceLesson() {
        // Create questions
        Question q1 = new Question(
                "q1",
                15, // Appears at 15 seconds
                "What is the capital of France?",
                "Paris",
                Arrays.asList("Paris", "London", "Berlin", "Madrid"),
                "Paris is the capital and largest city of France.");

        Question q2 = new Question(
                "q2",
                45, // Appears at 45 seconds
                "Which verb means 'to eat' in French?",
                "Manger",
                Arrays.asList("Boire", "Manger", "Dormir", "Parler"),
                "'Manger' means 'to eat' in French. 'Boire' means 'to drink'.");

        // Create video lesson
        VideoLesson lesson = new VideoLesson(
                "lesson_french_basics",
                "French Basics - Introduction",
                "Learn basic French vocabulary and phrases",
                "https://example.com/videos/french_basics.mp4",
                "https://example.com/thumbnails/french_basics.jpg",
                "A1",
                "Language Basics",
                120, // 2 minutes
                Arrays.asList(q1, q2));

        return lesson;
    }

    /**
     * Example 2: Create a video lesson with drag-and-drop questions
     */
    public VideoLesson createDragDropLesson() {
        // Drag & Drop Question: "I ____ to school every day."
        // Correct answer: "go"
        Question q1 = new Question(
                "q1",
                20,
                "I go to school every day", // Full sentence
                "go",
                Arrays.asList("go", "went", "going", "gone"),
                "'go' is the correct present simple form for daily routines.",
                1 // Blank at position 1 (0-based: I [blank] to school...)
        );

        // Another drag & drop: "She ____ a book yesterday."
        Question q2 = new Question(
                "q2",
                50,
                "She read a book yesterday",
                "read",
                Arrays.asList("read", "reads", "reading", "will read"),
                "'read' (past tense) is correct because of 'yesterday'.",
                1);

        VideoLesson lesson = new VideoLesson(
                "lesson_grammar_verbs",
                "English Grammar - Verb Tenses",
                "Practice using correct verb tenses",
                "https://example.com/videos/grammar_verbs.mp4",
                "https://example.com/thumbnails/grammar.jpg",
                "A2",
                "Grammar",
                90,
                Arrays.asList(q1, q2));

        return lesson;
    }

    /**
     * Example 3: Create a mixed lesson (both question types)
     */
    public VideoLesson createMixedLesson() {
        // Multiple choice question
        Question q1 = new Question(
                "q1",
                10,
                "Which greeting is most formal?",
                "How do you do?",
                Arrays.asList("Hey!", "Hi there", "How do you do?", "What's up?"),
                "'How do you do?' is the most formal greeting in English.");

        // Drag and drop question
        Question q2 = new Question(
                "q2",
                35,
                "Nice to meet you",
                "to",
                Arrays.asList("to", "too", "two", "at"),
                "'to' is the correct preposition in this phrase.",
                1);

        // Another multiple choice
        Question q3 = new Question(
                "q3",
                60,
                "How do you respond to 'How are you?'",
                "I'm fine, thank you",
                Arrays.asList("I'm fine, thank you", "Yes", "No", "Maybe"),
                "'I'm fine, thank you' is the standard polite response.");

        VideoLesson lesson = new VideoLesson(
                "lesson_greetings",
                "Greetings and Introductions",
                "Learn how to greet people properly in English",
                "https://example.com/videos/greetings.mp4",
                "https://example.com/thumbnails/greetings.jpg",
                "A1",
                "Conversation",
                75,
                Arrays.asList(q1, q2, q3));

        return lesson;
    }

    /**
     * Example 4: Upload video lesson to Firestore
     */
    public void uploadLessonToFirestore(VideoLesson lesson) {
        db.collection("videoLessons")
                .document(lesson.getId())
                .set(lesson)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ Video lesson uploaded successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Error uploading lesson: " + e.getMessage());
                });
    }

    /**
     * Example 5: Launch VideoLessonActivity from any Context
     */
    public void launchVideoLesson(android.content.Context context, String lessonId) {
        Intent intent = new Intent(context, VideoLessonActivity.class);
        intent.putExtra("lesson_id", lessonId);
        context.startActivity(intent);
    }

    /**
     * Example 6: Create and upload sample lessons for testing
     */
    public void createSampleLessons() {
        // Create 3 sample lessons
        VideoLesson lesson1 = createMultipleChoiceLesson();
        VideoLesson lesson2 = createDragDropLesson();
        VideoLesson lesson3 = createMixedLesson();

        // Upload to Firestore
        uploadLessonToFirestore(lesson1);
        uploadLessonToFirestore(lesson2);
        uploadLessonToFirestore(lesson3);
    }

    /**
     * Example 7: Load and display video lessons in RecyclerView
     * (Add this to your HomeFragment or dedicated VideoLessonsFragment)
     */
    public void loadVideoLessons() {
        db.collection("videoLessons")
                .whereEqualTo("level", "A1") // Filter by level
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VideoLesson> lessons = queryDocumentSnapshots.toObjects(VideoLesson.class);

                    // Display in RecyclerView
                    // adapter.updateData(lessons);

                    System.out.println("✅ Loaded " + lessons.size() + " video lessons");
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Error loading lessons: " + e.getMessage());
                });
    }

    /**
     * Example 8: Create a video lesson card click handler
     */
    public void onVideoLessonCardClicked(android.content.Context context, VideoLesson lesson) {
        // Update view count
        db.collection("videoLessons")
                .document(lesson.getId())
                .update("views", lesson.getViews() + 1);

        // Launch video lesson
        launchVideoLesson(context, lesson.getId());
    }
}

/*
 * ==========================================
 * USAGE EXAMPLE IN HomeFragment:
 * ==========================================
 * 
 * // In HomeFragment.java:
 * 
 * private void setupVideoLessonsSection() {
 * RecyclerView videoLessonsRv = view.findViewById(R.id.video_lessons_rv);
 * 
 * // Load video lessons from Firestore
 * db.collection("videoLessons")
 * .whereEqualTo("level", currentUserLevel)
 * .limit(5)
 * .get()
 * .addOnSuccessListener(snapshots -> {
 * List<VideoLesson> lessons = snapshots.toObjects(VideoLesson.class);
 * 
 * VideoLessonsAdapter adapter = new VideoLessonsAdapter(lessons, lesson -> {
 * // On click -> Launch VideoLessonActivity
 * Intent intent = new Intent(getContext(), VideoLessonActivity.class);
 * intent.putExtra("lesson_id", lesson.getId());
 * startActivity(intent);
 * });
 * 
 * videoLessonsRv.setLayoutManager(
 * new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
 * );
 * videoLessonsRv.setAdapter(adapter);
 * });
 * }
 * 
 * ==========================================
 * FIRESTORE CONSOLE QUICK ADD:
 * ==========================================
 * 
 * 1. Go to Firebase Console > Firestore
 * 2. Create collection "videoLessons"
 * 3. Add document with auto-ID or custom ID
 * 4. Copy-paste this JSON structure:
 * 
 * {
 * "id": "lesson_001",
 * "title": "Daily Conversations - At the Restaurant",
 * "description": "Learn common restaurant phrases",
 * "videoUrl":
 * "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
 * "thumbnailUrl": "https://example.com/thumb.jpg",
 * "level": "A2",
 * "category": "Conversation",
 * "duration": 120,
 * "views": 0,
 * "createdAt": 1702345678900,
 * "questions": [
 * {
 * "id": "q1",
 * "type": "MULTIPLE_CHOICE",
 * "appearAtSecond": 10,
 * "questionText": "What does 'May I take your order?' mean?",
 * "correctAnswer": "The waiter is ready to take your food request",
 * "options": [
 * "The waiter is ready to take your food request",
 * "The waiter wants you to leave",
 * "The waiter is angry",
 * "The waiter is busy"
 * ],
 * "explanation":
 * "This is a polite phrase waiters use to ask what you want to eat."
 * }
 * ]
 * }
 * 
 * 5. Click "Save"
 * 6. Test by launching VideoLessonActivity with this lesson_id
 * 
 */
