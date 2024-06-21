package com.G2T5203.wingit.booking;

import com.G2T5203.wingit.entities.Booking;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BookingSimpleJson {
    private Integer bookingId;
    private String username;

    // Outbound Routelisting
    private String outboundPlaneId;
    private int outboundRouteId;
    private Date outboundDepartureDatetime;

    // Inbound Routelisting
    private String inboundPlaneId;
    private int inboundRouteId;
    private Date inboundDepartureDatetime;

    // The rest
    private Date startBookingDatetime;
    private int partySize;
    private double chargedPrice;
    private boolean isPaid;

    // SeatListing, just a Json list of the seat numbers for each route listing
    private List<String> outboundSeatNumbers;
    private List<String> inboundSeatNumbers;

    public BookingSimpleJson(Integer bookingId, String username, String outboundPlaneId, int outboundRouteId, Date outboundDepartureDatetime, String inboundPlaneId, int inboundRouteId, Date inboundDepartureDatetime, Date startBookingDatetime, int partySize, double chargedPrice, boolean isPaid, List<String> outboundSeatNumbers, List<String> inboundSeatNumbers) {
        this.bookingId = bookingId;
        this.username = username;

        this.outboundPlaneId = outboundPlaneId;
        this.outboundRouteId = outboundRouteId;
        this.outboundDepartureDatetime = outboundDepartureDatetime;

        this.inboundPlaneId = inboundPlaneId;
        this.inboundRouteId = inboundRouteId;
        this.inboundDepartureDatetime = inboundDepartureDatetime;

        this.startBookingDatetime = startBookingDatetime;
        this.partySize = partySize;
        this.chargedPrice = chargedPrice;
        this.isPaid = isPaid;

        this.outboundSeatNumbers = outboundSeatNumbers;
        this.inboundSeatNumbers = inboundSeatNumbers;
    }

    public BookingSimpleJson(Booking booking) {
        this(
                booking.getBookingId(),
                booking.getWingitUser().getUsername(),

                booking.getOutboundRouteListing().getRouteListingPk().getPlane().getPlaneId(),
                booking.getOutboundRouteListing().getRouteListingPk().getRoute().getRouteId(),
                booking.getOutboundRouteListing().getRouteListingPk().getDepartureDatetime(),

                booking.hasInboundRouteListing() ? booking.getInboundRouteListing().getRouteListingPk().getPlane().getPlaneId() : null,
                booking.hasInboundRouteListing() ? booking.getInboundRouteListing().getRouteListingPk().getRoute().getRouteId() : -1,
                booking.hasInboundRouteListing() ? booking.getInboundRouteListing().getRouteListingPk().getDepartureDatetime() : null,

                booking.getStartBookingDatetime(),
                booking.getPartySize(),
                booking.getChargedPrice(),
                booking.isPaid(),

                // Check if seat listing matches outbound route listing's planeId, routeId, departureDatetime
                // If yes, add the seatNumber to the outbound list
                // Repeat for inbound route listing
                booking.getSeatListing().stream()
                        .filter(s -> s.getSeatListingPk().checkSeatBelongsToRouteListing(s, booking.getOutboundRouteListing().getRouteListingPk()))
                        .map(seatListing -> seatListing.getSeatListingPk().getSeat().getSeatPk().getSeatNumber())
                        .collect(Collectors.toList()),
                booking.getSeatListing().stream()
                        .filter(s -> s.getSeatListingPk().checkSeatBelongsToRouteListing(s, booking.getInboundRouteListing().getRouteListingPk()))
                        .map(seatListing -> seatListing.getSeatListingPk().getSeat().getSeatPk().getSeatNumber())
                        .collect(Collectors.toList())
        );
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOutboundPlaneId() {
        return outboundPlaneId;
    }

    public void setOutboundPlaneId(String outboundPlaneId) {
        this.outboundPlaneId = outboundPlaneId;
    }

    public int getOutboundRouteId() {
        return outboundRouteId;
    }

    public void setOutboundRouteId(int outboundRouteId) {
        this.outboundRouteId = outboundRouteId;
    }

    public Date getOutboundDepartureDatetime() {
        return outboundDepartureDatetime;
    }

    public void setOutboundDepartureDatetime(Date outboundDepartureDatetime) {
        this.outboundDepartureDatetime = outboundDepartureDatetime;
    }

    public String getInboundPlaneId() {
        return inboundPlaneId;
    }

    public void setInboundPlaneId(String inboundPlaneId) {
        this.inboundPlaneId = inboundPlaneId;
    }

    public int getInboundRouteId() {
        return inboundRouteId;
    }

    public void setInboundRouteId(int inboundRouteId) {
        this.inboundRouteId = inboundRouteId;
    }

    public Date getInboundDepartureDatetime() {
        return inboundDepartureDatetime;
    }

    public void setInboundDepartureDatetime(Date inboundDepartureDatetime) {
        this.inboundDepartureDatetime = inboundDepartureDatetime;
    }

    public Date getStartBookingDatetime() {
        return startBookingDatetime;
    }

    public void setStartBookingDatetime(Date startBookingDatetime) {
        this.startBookingDatetime = startBookingDatetime;
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public double getChargedPrice() {
        return chargedPrice;
    }

    public void setChargedPrice(double chargedPrice) {
        this.chargedPrice = chargedPrice;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public List<String> getOutboundSeatNumbers() {
        return outboundSeatNumbers;
    }

    public void setOutboundSeatNumbers(List<String> outboundSeatNumbers) {
        this.outboundSeatNumbers = outboundSeatNumbers;
    }

    public List<String> getInboundSeatNumbers() {
        return inboundSeatNumbers;
    }

    public void setInboundSeatNumbers(List<String> inboundSeatNumbers) {
        this.inboundSeatNumbers = inboundSeatNumbers;
    }
}
