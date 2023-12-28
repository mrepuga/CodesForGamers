package com.example.user.entities;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="`cart_item`")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "order_cart_id")
    @JsonIgnore
    private OrderCart orderCart;

    @Column
    private Long gameId;

    @Column
    private String platform;

    @Column
    private Float price;

    @Column
    private String gameName;

    @Column
    private String code;
}
