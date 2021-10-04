package com.smalik.choreographer;

import com.smalik.choreographer.api.TurnRequest;

import java.util.Comparator;
import java.util.TreeSet;

public class WaitingRequestList extends TreeSet<TurnRequest> {
    public WaitingRequestList() {
        super(Comparator.comparing(TurnRequest::getTime));
    }
}
