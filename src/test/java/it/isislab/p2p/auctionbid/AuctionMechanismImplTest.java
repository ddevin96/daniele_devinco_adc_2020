package it.isislab.p2p.auctionbid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.junit.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class AuctionMechanismImplTest {
    
    static AuctionMechanismImpl peer0, peer1, peer2;

    public AuctionMechanismImplTest() {

    }    

    /*
    call before all test happen
    */
    @BeforeAll
    public static void setup() throws Exception{
        class MessageListenerImpl implements MessageListener{
			int peerid;
		
			public MessageListenerImpl(int peerid) {
				this.peerid=peerid;
            }
            
			public Object parseMessage(Object obj) {
				System.out.println("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
				return "success";
			}
        }
        
        peer0 = new AuctionMechanismImpl(0, "127.0.0.1", new MessageListenerImpl(0));
        peer1 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerImpl(1));
        peer2 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerImpl(1));

    }

    @Test
    void testCreateBid(TestInfo testInfo) {
        assertTrue(peer0.createAuction("cane", new Date(), 100.0, "bel cane"));
    }

    @Test
    void testCheckRunningBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("canenuovo", newDate, 100.0, "bel cane nuovo");
            //Thread.sleep(7000);
            assertEquals("THIS AUCTION IS STILL RUNNING\n" + "canenuovo", peer1.checkAuction("canenuovo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCheckExpiredBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miao", newDate, 100.0, "bel gatto"));
            //Thread.sleep(5000);
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miao", peer1.checkAuction("miao"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateSameBid(TestInfo testInfo) {
        try {
            peer0.createAuction("cane", new Date(), 100.0, "bel cane");
            //Thread.sleep(7000);
            assertFalse(peer1.createAuction("cane", new Date(), 150.0, "altro cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateBidNegativePrice(TestInfo testInfo) {
        try {
            assertFalse(peer0.createAuction("caneNegativo", new Date(), -100.0, "bel cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateBidPastDate(TestInfo testInfo) {
        try {
            assertFalse(peer0.createAuction("canePassato", new Date(2000, 10, 10), 100.0, "bel cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testListAllBids(TestInfo testInfo) {
        peer0.createAuction("auto", new Date(), 100.0, "bel cane");
        peer1.createAuction("casa", new Date(), 100.0, "bella casa");
        peer0.createAuction("libro", new Date(), 100.0, "bel libro");
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("auto");
        arr.add("casa");
        arr.add("libro");
        assertEquals(arr, peer0.listAuctions());
    }
    
    @Test
    void testLeaveNetwork(){
        assertTrue(peer2.leaveNetwork());
    }

}
