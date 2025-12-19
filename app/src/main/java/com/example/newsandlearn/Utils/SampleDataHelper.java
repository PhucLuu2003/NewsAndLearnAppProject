package com.example.newsandlearn.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to seed sample data into Firestore for testing and first-run experience.
 */
public class SampleDataHelper {

    private static final String TAG = "SampleDataHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface SeedCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Alias for backward compatibility with MainActivity
    public interface OnCompleteListener extends SeedCallback {}

    public void generateAllSampleData(OnCompleteListener listener) {
        // Use a dummy context or pass application context if possible, but here we just pass null 
        // as Context is only used for Toast. Be careful to check context nullity in seeVocabularyData.
        // Actually, seedVocabularyData requires context.
        // Let's create a overloaded method or just implement simple logic here.
        
        // Since MainActivity calls this without context, let's just run the seeding logic but 
        // suppress Toasts if context is null.
        seedVocabularyData(null, listener);
    }

    public SampleDataHelper() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void seedVocabularyData(Context context, SeedCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        List<Vocabulary> sampleWords = createSampleVocabulary();

        WriteBatch batch = db.batch();

        // 1. Add words to Global 'vocabularies' collection (if not exists is tricky with batch, so we just overwrite/set)
        // Ideally we check first, but for seeding sample data, we can just ensure they exist.
        for (Vocabulary vocab : sampleWords) {
            // Use word as ID for simplicity or generate new ID
            String vocabId = vocab.getWord().toLowerCase().replaceAll("\\s+", "_");
            vocab.setId(vocabId);
            batch.set(db.collection("vocabularies").document(vocabId), vocab);

            // 2. Add progress to 'users/{userId}/user_vocabulary'
            UserVocabulary userVocab = new UserVocabulary(vocabId);
            userVocab.setMastery(0); // Start fresh
            userVocab.setNextReview(new java.util.Date()); // Review now
            
            batch.set(db.collection("users").document(userId)
                    .collection("user_vocabulary").document(vocabId), userVocab);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Seeding successful");
                    if (context != null) {
                        Toast.makeText(context, "Sample data added!", Toast.LENGTH_SHORT).show();
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Seeding failed", e);
                    if (context != null) {
                        Toast.makeText(context, "Error adding sample data", Toast.LENGTH_SHORT).show();
                    }
                    callback.onFailure(e);
                });
    }

    private List<Vocabulary> createSampleVocabulary() {
        List<Vocabulary> list = new ArrayList<>();

        list.add(new Vocabulary(null, "Epiphany", "Sự giác ngộ", "/ɪˈpɪf.ən.i/", "noun",
                "He had an epiphany that changed his life forever.", "Anh ấy có một sự giác ngộ đã thay đổi cuộc đời mãi mãi.",
                "C2", "General"));

        list.add(new Vocabulary(null, "Serendipity", "Sự tình cờ may mắn", "/ˌser.ənˈdɪp.ə.ti/", "noun",
                "Meeting her was pure serendipity.", "Gặp cô ấy hoàn toàn là sự tình cờ may mắn.",
                "C2", "Romance"));

        list.add(new Vocabulary(null, "Resilience", "Khả năng phục hồi", "/rɪˈzɪl.jəns/", "noun",
                "She showed great resilience in the face of adversity.", "Cô ấy thể hiện khả năng phục hồi tuyệt vời khi đối mặt với nghịch cảnh.",
                "C1", "Personality"));

        list.add(new Vocabulary(null, "Eloquent", "Hùng biện, lưu loát", "/ˈel.ə.kwənt/", "adjective",
                "He gave an eloquent speech to the audience.", "Anh ấy đã có một bài phát biểu hùng hồn trước khán giả.",
                "C1", "Communication"));

        list.add(new Vocabulary(null, "Mellifluous", "Ngọt ngào, êm tai", "/məˈlɪf.lu.əs/", "adjective",
                "She has a rich, mellifluous voice.", "Cô ấy có giọng nói trầm ấm, ngọt ngào.",
                "C2", "Arts"));

        list.add(new Vocabulary(null, "Ineffable", "Không thể tả được", "/ɪnˈef.ə.bəl/", "adjective",
                "The beauty of the sunset was ineffable.", "Vẻ đẹp của hoàng hôn là không thể tả xiết.",
                "C2", "Nature"));
        
        list.add(new Vocabulary(null, "Ephemeral", "Phù du, ngắn ngủi", "/ɪˈfem.ər.əl/", "adjective",
                "Fashions are ephemeral, changing with every season.", "Thời trang là phù du, thay đổi theo từng mùa.",
                "C2", "Philosophy"));

        list.add(new Vocabulary(null, "Luminous", "Sáng chói, dạ quang", "/ˈluː.mə.nəs/", "adjective",
                "The room was luminous with sunlight.", "Căn phòng rực rỡ ánh nắng.",
                "C1", "Nature"));
        
        list.add(new Vocabulary(null, "Solitude", "Sự cô độc (tích cực)", "/ˈsɒl.ɪ.tjuːd/", "noun",
                "He enjoyed the peace and solitude of the woods.", "Anh ấy tận hưởng sự bình yên và tĩnh lặng của khu rừng.",
                "B2", "Lifestyle"));
        
        list.add(new Vocabulary(null, "Quintessential", "Tinh túy, điển hình", "/ˌkwɪn.tɪˈsen.ʃəl/", "adjective",
                "Sheep's milk cheese is the quintessential Corsican cheese.", "Phô mai sữa cừu là loại phô mai tinh túy của vùng Corsica.",
                "C2", "General"));

        return list;
    }
}
