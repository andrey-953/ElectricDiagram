package horizonSimple.electrical.packet;

import java.util.List;

public interface ElementsPacket {
    public String getName();
    public List<ElementInfo> getElements();
}
