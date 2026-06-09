package com.example.walking.domain;

/** region 테이블 — 시·도 / 시·군·구 */
public class Region {
    private int regionId;
    private String sido;
    private String sigungu;

    public Region() {
    }

    public int getRegionId() { return regionId; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }

    public void setRegionId(int regionId) { this.regionId = regionId; }
    public void setSido(String sido) { this.sido = sido; }
    public void setSigungu(String sigungu) { this.sigungu = sigungu; }
}
