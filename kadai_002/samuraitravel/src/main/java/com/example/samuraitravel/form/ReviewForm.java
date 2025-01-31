package com.example.samuraitravel.form;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewForm {
	private Integer id;
	private Integer houseId;
    private Integer rating; // 星の評価
    private String comment; // コメント内容
    private String review;
    private Integer reservationId;
    
    public void setReservationId(Integer reservationId) { // 追加
        this.reservationId = reservationId;
    }

 // デフォルトコンストラクタ（引数なし）
    public ReviewForm() {
    }
    
}
