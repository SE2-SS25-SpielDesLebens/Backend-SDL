package at.aau.serg.websocketserver.session.payout;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PayoutRepository {

    private PayoutRepository() {
        // verhindert Instanziierung dieser Utility-Klasse
    }

    @Getter
    public static class PayoutEntry {
        private final int payoutId;

        @Setter
        private boolean allowPayout;

        public PayoutEntry(int payoutId, boolean allowPayout) {
            this.payoutId = payoutId;
            this.allowPayout = allowPayout;
        }
    }

    @Getter
    private static class PayoutExport {
        @SerializedName("payouts")
        private List<PayoutEntry> payouts;
    }

    public static List<PayoutEntry> createPayoutListFromJson(String json) {
        Gson gson = new Gson();
        PayoutExport export = gson.fromJson(json, PayoutExport.class);
        return export.getPayouts();
    }
}
