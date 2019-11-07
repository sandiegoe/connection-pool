package com.arexstorm.pool.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A PoolTest.
 */
@Entity
@Table(name = "pool_test")
public class PoolTest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 5)
    @Column(name = "url", length = 5)
    private String url;

    @Size(max = 10)
    @Column(name = "pool_size", length = 10)
    private String poolSize;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public PoolTest name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public PoolTest url(String url) {
        this.url = url;
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPoolSize() {
        return poolSize;
    }

    public PoolTest poolSize(String poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public void setPoolSize(String poolSize) {
        this.poolSize = poolSize;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PoolTest poolTest = (PoolTest) o;
        if (poolTest.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), poolTest.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PoolTest{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", url='" + getUrl() + "'" +
            ", poolSize='" + getPoolSize() + "'" +
            "}";
    }
}
