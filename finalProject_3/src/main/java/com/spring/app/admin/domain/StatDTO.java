package com.spring.app.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Getter, Setter, ToString 등을 자동 생성 (Lombok 사용 시)
@AllArgsConstructor  // 모든 필드를 포함한 생성자
@NoArgsConstructor   // 기본 생성자
public class StatDTO {
    private String label; // 차트의 X축 (예: "03/25" 또는 "2024-03")
    private int count;    // 차트의 Y축 (예: 가입자 수 15명)
}
