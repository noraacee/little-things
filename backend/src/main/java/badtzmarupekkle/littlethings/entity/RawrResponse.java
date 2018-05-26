package badtzmarupekkle.littlethings.entity;

import java.util.List;

public class RawrResponse extends Response {
    private List<Rawr> rawrs;
    private Rawr rawr;

    public List<Rawr> getRawrs() {
        return rawrs;
    }
    public void setRawrs(List<Rawr> rawrs) {
        this.rawrs = rawrs;
    }
    public Rawr getRawr() {
        return rawr;
    }
    public void setRawr(Rawr rawr) {
        this.rawr = rawr;
    }
}
