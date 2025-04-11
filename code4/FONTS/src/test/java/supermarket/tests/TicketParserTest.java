package supermarket.tests;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import supermarket.domain.relations.TicketParser;
import supermarket.domain.relations.RelationsTickets;
import supermarket.tuples.ProductInfo;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TicketParserTest {

    private TicketParser ticketParser;
    private RelationsTickets relationsTicketsMock;

    @Before
    public void setUp() {
        relationsTicketsMock = Mockito.mock(RelationsTickets.class);
        ticketParser = new TicketParser(relationsTicketsMock);
    }

    @Test
    public void testNonExistentTicket() {
        HashSet<Integer> result = ticketParser.readTicket("nonexistent.txt"); // File path doesn't exist
        assertNull("Expected null for nonexistent ticket", result);
    }

    @Test
    public void testInvalidTicket() {
        HashSet<Integer> result = ticketParser.readTicket("src/test/resources/testingTickets/invalidTicket.txt"); // Invalid barcode format
        assertNull("Expected null for invalid ticket", result);
    }

    @Test
    public void testNonExistentProduct() {
        when(relationsTicketsMock.getProductInfo(anyInt())).thenThrow(new RuntimeException("Product not found"));

        try {
            ticketParser.readTicket("src/test/resources/testingTickets/nonExistingBarcodeTicket.txt"); // Ticket with non-existent barcode
            fail("Expected exception for non-existent product");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void testValidTicket() {
        HashSet<Integer> expected = new HashSet<>();
        for (int i = 1; i <= 6; i++) {
            expected.add(i);
            when(relationsTicketsMock.getProductInfo(i)).thenReturn(new ProductInfo(i, "p" + i, 0, "a"));
        }
        HashSet<Integer> result = ticketParser.readTicket("src/test/resources/testingTickets/validTicket.txt");
        assertEquals(expected, result);
    }

    @Test
    public void testValidTicketWithDuplicates() {
        HashSet<Integer> expected = new HashSet<>();
        for (int i = 1; i <= 6; i++) {
            expected.add(i);
            when(relationsTicketsMock.getProductInfo(i)).thenReturn(new ProductInfo(i, "p" + i, 0, "a"));
        }
        HashSet<Integer> result = ticketParser.readTicket("src/test/resources/testingTickets/validTicketWithDuplicates.txt");
        assertEquals("Expected valid ticket result with duplicates removed", expected, result);
    }

    @Test
    public void testIncorrectName() {
        when(relationsTicketsMock.getProductInfo(anyInt())).thenThrow(new RuntimeException("Incorrect product name"));
        try {
            ticketParser.readTicket("src/test/resources/testingTickets/incorrectProductNameTicket.txt");
            fail("Expected exception for incorrect product name");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void testEmptyTicket() {
        HashSet<Integer> result = ticketParser.readTicket("src/test/resources/testingTickets/emptyTicket.txt");
        assertNotNull("Expected non-null result for empty ticket", result);
        assertTrue("Expected empty result for empty ticket", result.isEmpty());
    }
}
