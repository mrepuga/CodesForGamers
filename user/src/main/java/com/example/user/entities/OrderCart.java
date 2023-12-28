package com.example.user.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="`order_cart`")
public class OrderCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private LocalDateTime purchaseDateTime;

    @Column
    private Float totalPrice;

    @OneToMany(mappedBy = "orderCart", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;



}
