package at.aau.serg.websocketserver.session.payout;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

 class PayoutRepositoryTest {

    @Test
     void testPayoutEntryFieldsAndSetters() {
        PayoutRepository.PayoutEntry entry = new PayoutRepository.PayoutEntry(42, false);

        assertEquals(42, entry.getPayoutId());
        assertFalse(entry.isAllowPayout());

        entry.setAllowPayout(true);
        assertTrue(entry.isAllowPayout());
    }

    @Test
     void testCreatePayoutListFromJson_validJson() {
        String json = "{ \"payouts\": [ " +
                "{ \"payoutId\": 5, \"allowPayout\": true }, " +
                "{ \"payoutId\": 10, \"allowPayout\": false } " +
                "] }";

        List<PayoutRepository.PayoutEntry> entries = PayoutRepository.createPayoutListFromJson(json);

        assertNotNull(entries);
        assertEquals(2, entries.size());

        PayoutRepository.PayoutEntry first = entries.get(0);
        PayoutRepository.PayoutEntry second = entries.get(1);

        assertEquals(5, first.getPayoutId());
        assertTrue(first.isAllowPayout());

        assertEquals(10, second.getPayoutId());
        assertFalse(second.isAllowPayout());
    }

    @Test
     void testCreatePayoutListFromJson_emptyList() {
        String json = "{ \"payouts\": [] }";
        List<PayoutRepository.PayoutEntry> entries = PayoutRepository.createPayoutListFromJson(json);

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
     void testCreatePayoutListFromJson_nullJsonField() {
        String json = "{}";
        List<PayoutRepository.PayoutEntry> entries = PayoutRepository.createPayoutListFromJson(json);

        assertNull(entries); // in deiner Implementation gibt getPayouts() null zurück
    }
}
