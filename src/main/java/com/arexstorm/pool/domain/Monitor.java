package com.arexstorm.pool.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A Monitor.
 */
@Entity
@Table(name = "monitor")
public class Monitor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "test", length = 100, nullable = false)
    private String test;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTest() {
        return test;
    }

    public Monitor test(String test) {
        this.test = test;
        return this;
    }

    public void setTest(String test) {
        this.test = test;
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
        Monitor monitor = (Monitor) o;
        if (monitor.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), monitor.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Monitor{" +
            "id=" + getId() +
            ", test='" + getTest() + "'" +
            "}";
    }
}
