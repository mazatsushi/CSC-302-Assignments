/*===============================================================*
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Author: Professor SUN Chengzheng                             *
 *          School of Computer Engineering                       *
 *          Nanyang Technological University                     *
 *          Singapore 639798                                     *
 *===============================================================*/

public class SWP {

    /*
     * ========================================================================*
     * the following are provided, do not change them!!
     * ========================================================================
     */
    //the following are protocol constants.
    public static final int MAX_SEQ = 7;
    public static final int NR_BUFS = (MAX_SEQ + 1) / 2;
    // the following are protocol variables
    private boolean no_nak = true;
    private int oldest_frame = 0;
    private PEvent event = new PEvent();
    private Packet out_buf[] = new Packet[NR_BUFS];
    //the following are used for simulation purpose only
    private SWE swe = null;
    private String sid = null;

    //Constructor
    public SWP(SWE sw, String s) {
        swe = sw;
        sid = s;
    }

    //the following methods are all protocol related
    private void init() {
        for (int i = 0; i < NR_BUFS; i++) {
            out_buf[i] = new Packet();
        }
    }

    private void wait_for_event(PEvent e) {
        swe.wait_for_event(e); //may be blocked
        oldest_frame = e.seq;  //set timeout frame seq
    }

    private void enable_network_layer(int nr_of_bufs) {
        //network layer is permitted to send if credit is available
        swe.grant_credit(nr_of_bufs);
    }

    private void from_network_layer(Packet p) {
        swe.from_network_layer(p);
    }

    private void to_network_layer(Packet packet) {
        swe.to_network_layer(packet);
    }

    private void to_physical_layer(PFrame fm) {
        System.out.println("SWP: Sending frame: seq = " + fm.seq
                + " ack = " + fm.ack + " kind = "
                + PFrame.KIND[fm.kind] + " info = " + fm.info.data);
        System.out.flush();
        swe.to_physical_layer(fm);
    }

    private void from_physical_layer(PFrame fm) {
        PFrame fm1 = swe.from_physical_layer();
        fm.kind = fm1.kind;
        fm.seq = fm1.seq;
        fm.ack = fm1.ack;
        fm.info = fm1.info;
    }


    /*
     * ===========================================================================*
     * implement your Protocol Variables and Methods below:
     * ==========================================================================
     */
    public void protocol6() {
        init();
        int ack_expected = 0; // Lower edge of sender's window
        int next_frame_to_send = 0; // Upper edge of sender's window
        int frame_expected = 0; // Lower edge of receiver's window
        int too_far = NR_BUFS; // Upper edge of receiver's window
        int nbuffered = 0; // How many output buffers currently used
        PFrame r = new PFrame(); // Scratch variable

        Packet in_buf[] = new Packet[NR_BUFS]; // Buffers for the inbound stream        
        // Initialize the buffers for the inbound stream
        for (int j = 0; j < NR_BUFS; j++) {
            out_buf[j] = new Packet();
        }

        boolean[] arrived = new boolean[NR_BUFS]; // Inbound bit map
        // Initialize the inbound bit map
        for (int j = 0; j < NR_BUFS; j++) {
            arrived[j] = false;
        }

        enable_network_layer(NR_BUFS); // Initialize

        while (true) {
            wait_for_event(event);
            switch (event.type) {
                // Accept, save and transmit a new frame
                case (PEvent.NETWORK_LAYER_READY):
                    nbuffered++; // Expand the window
                    from_network_layer(out_buf[next_frame_to_send % NR_BUFS]); // Fetch new packet
                    send_frame(PFrame.DATA, next_frame_to_send, frame_expected, out_buf); // Transmit the frame
                    next_frame_to_send = ((++next_frame_to_send) % (NR_BUFS + 1));
                    break;

                // A data or control frame has arrived
                case (PEvent.FRAME_ARRIVAL):
                    from_physical_layer(r); // Fetch incoming frame from physical layer

                    if (r.kind == PFrame.DATA) {
                        // An undamaged frame has arrived
                        if ((r.seq != frame_expected) && no_nak) {
                            send_frame(PFrame.NAK, 0, frame_expected, out_buf);
                        } else {
                            start_ack_timer();
                        }
                    }

                    if ((between(frame_expected, r.seq, too_far)) && ((arrived[r.seq % NR_BUFS]) == false)) {
                        // Frames may be accepted in any order
                        arrived[r.seq % NR_BUFS] = true; // Mark buffer as full
                        in_buf[r.seq % NR_BUFS] = r.info; // Insert data into buffer
                        while (arrived[frame_expected % NR_BUFS]) {
                            // Pass frames and advance window
                            to_network_layer(in_buf[frame_expected % NR_BUFS]);
                            no_nak = true;
                            arrived[frame_expected % NR_BUFS] = false;
                            frame_expected = (++frame_expected % (NR_BUFS + 1)); // Advance lower edge of receiver's window
                            too_far = (++too_far % (NR_BUFS + 1)); // Advance upper edge of receiver's window
                            start_ack_timer(); // To see if a separate ACK is needed
                        }
                    }

                    if ((r.kind == PFrame.NAK) && between(ack_expected, (r.ack + 1) % (MAX_SEQ + 1), next_frame_to_send)) {
                        send_frame(PFrame.DATA, (r.ack + 1) % (MAX_SEQ + 1), frame_expected, out_buf);
                    }
                    
                    while(between(ack_expected, r.ack, next_frame_to_send)) {
                        nbuffered--; // Handle piggybacked ACK
                        stop_timer(ack_expected % NR_BUFS); // Frame arrived intact
                        ack_expected = (++ack_expected % (NR_BUFS + 1)); // Advance lower edge of sender's window
                    }

                    break;

                case (PEvent.CKSUM_ERR):
                    if (no_nak) {
                        // Damaged frame
                        send_frame(PFrame.NAK, 0, frame_expected, out_buf);
                    }
                    break;

                case (PEvent.TIMEOUT):
                    // We timed out
                    swe.generate_timeout_event(oldest_frame);
                    send_frame(PFrame.DATA, oldest_frame, frame_expected, out_buf);
                    break;

                case (PEvent.ACK_TIMEOUT):
                    // ACK timer expired, send ACK
                    swe.generate_acktimeout_event();
                    send_frame(PFrame.ACK, 0, frame_expected, out_buf);
                    break;
                
                default:
                    System.out.println("SWP: undefined event type = "
                            + event.type);
                    System.out.flush();
            }
            
            if (nbuffered < NR_BUFS) {
                enable_network_layer(NR_BUFS);
            }
        }
    }

    boolean between(int a, int b, int c) {
        // Same as between() in protocol 5, but shorter and more obscure
        return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
    }

    void send_frame(int fk, int frame_nr, int frame_expected, Packet[] buffer) {
        // Construct and send a data, ACK or NAK frame
        PFrame s = new PFrame(); // Scratch variable
        s.kind = fk; // kind == data, ACK or NAK

        if (fk == PFrame.DATA) {
            s.info = buffer[frame_nr % NR_BUFS];
        }
        s.seq = frame_nr; // Only meaningful for data frames
        s.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);

        if (fk == PFrame.NAK) {
            no_nak = false; // One NAK per frame only
        }

        to_physical_layer(s); // Transmit the frame

        if (fk == PFrame.DATA) {
            start_timer(frame_nr % NR_BUFS);
        }
        stop_ack_timer(); // No need for separate ACK frame
    }

    /*
     * Note: when start_timer() and stop_timer() are called, the "seq" parameter
     * must be the sequence number, rather than the index of the timer array, of
     * the frame associated with this timer,
     */
    private void start_timer(int seq) {
    }

    private void stop_timer(int seq) {
    }

    private void start_ack_timer() {
    }

    private void stop_ack_timer() {
    }
}//End of class

/*
 * Note: In class SWE, the following two public methods are available: .
 * generate_acktimeout_event() and . generate_timeout_event(seqnr).
 *
 * To call these two methods (for implementing timers), the "swe" object should
 * be referred as follows: swe.generate_acktimeout_event(), or
 * swe.generate_timeout_event(seqnr).
 */
