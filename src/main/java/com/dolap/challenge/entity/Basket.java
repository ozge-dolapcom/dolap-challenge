package com.dolap.challenge.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Basket {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "basket",
            fetch = FetchType.EAGER,
            cascade =  CascadeType.ALL,
            orphanRemoval = true)
    private List<BasketItem> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public void setItems(List<BasketItem> items) {
        this.items = items;
    }
}
