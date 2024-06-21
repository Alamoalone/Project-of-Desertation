package com.G2T5203.wingit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingId;
    @ManyToOne
    @JoinColumn(name = "username")
    private WingitUser wingitUser;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "outboundPlaneId", referencedColumnName = "planeId"),
            @JoinColumn(name = "outboundRouteId", referencedColumnName = "routeId"),
            @JoinColumn(name = "outboundDepartureDatetime", referencedColumnName = "departureDatetime")
    })
    private RouteListing outboundRouteListing;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "inboundPlaneId", referencedColumnName = "planeId"),
            @JoinColumn(name = "inboundRouteId", referencedColumnName = "routeId"),
            @JoinColumn(name = "inboundDepartureDatetime", referencedColumnName = "departureDatetime")
    })
    private RouteListing inboundRouteListing;
    private Date startBookingDatetime;
    private int partySize;
    private double chargedPrice;
    private boolean isPaid;
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    //@JsonManagedReference
    private List<SeatListing> seatListing;

    public Booking(Integer bookingId, WingitUser wingitUser, RouteListing outboundRouteListing, RouteListing inboundRouteListing, Date startBookingDatetime, int partySize, double chargedPrice, boolean isPaid) {
        this.bookingId = bookingId;
        this.wingitUser = wingitUser;
        this.outboundRouteListing = outboundRouteListing;
        this.inboundRouteListing = inboundRouteListing;
        this.startBookingDatetime = startBookingDatetime;
        this.partySize = partySize;
        this.chargedPrice = chargedPrice;
        this.isPaid = isPaid;
    }

    public Booking(WingitUser wingitUser, RouteListing outboundRouteListing, RouteListing inboundRouteListing, Date startBookingDatetime, int partySize, double chargedPrice, boolean isPaid) {
        this.wingitUser = wingitUser;
        this.outboundRouteListing = outboundRouteListing;
        this.inboundRouteListing = inboundRouteListing;
        this.startBookingDatetime = startBookingDatetime;
        this.partySize = partySize;
        this.chargedPrice = chargedPrice;
        this.isPaid = isPaid;
    }

    public Booking() {

    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public WingitUser getWingitUser() {
        return wingitUser;
    }

    public void setWingitUser(WingitUser wingitUser) {
        this.wingitUser = wingitUser;
    }

    public RouteListing getOutboundRouteListing() {
        return outboundRouteListing;
    }

    public void setOutboundRouteListing(RouteListing outboundRouteListing) {
        this.outboundRouteListing = outboundRouteListing;
    }

    public RouteListing getInboundRouteListing() {
        return inboundRouteListing;
    }

    public void setInboundRouteListing(RouteListing inboundRouteListing) {
        this.inboundRouteListing = inboundRouteListing;
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

    public List<SeatListing> getSeatListing() {
        return seatListing;
    }

    public void setSeatListing(List<SeatListing> seatListing) {
        this.seatListing = seatListing;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", wingitUser=" + wingitUser +
                ", outboundRouteListing=" + outboundRouteListing +
                ", inboundRouteListing=" + inboundRouteListing +
                ", startBookingDatetime=" + startBookingDatetime +
                ", partySize=" + partySize +
                ", chargedPrice=" + chargedPrice +
                ", isPaid=" + isPaid +
                '}';
    }
}
