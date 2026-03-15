package com.spring.app.admin.domain;

import lombok.Data;
import java.util.Date;

@Data
public class ReviewAdminDTO {
    private int reviewNo;
    private String email;
    private String nickname;
    private double rating;
    private String reviewContent;
    private String oneLineCat;
    private Date createdAt;
    private int flagged; // 0=정상

    public boolean isFlagged() {
        return flagged == 1;
    }
}
