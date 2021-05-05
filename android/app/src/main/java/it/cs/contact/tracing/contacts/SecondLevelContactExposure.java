package it.cs.contact.tracing.contacts;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import it.cs.contact.tracing.model.entity.DeviceTrace;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SecondLevelContactExposure implements Runnable {

    final ConcurrentMap<String, List<DeviceTrace>> myContacts;
    final Set<String> secondLevelContactsKeys;

    @Override
    public void run() {

    }
}
