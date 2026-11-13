package com.ecommerce.platform.cms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "product_media",
    indexes = @Index(name = "idx_product_media_product_id", columnList = "product_id")
)
public class ProductMedia extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "media_url", nullable = false, length = 2048)
    private String mediaUrl;

    @Column(name = "alt_text", length = 500)
    private String altText;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private ProductMediaType mediaType;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
