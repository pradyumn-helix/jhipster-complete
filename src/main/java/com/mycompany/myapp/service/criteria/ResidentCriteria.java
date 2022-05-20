package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Resident} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.ResidentResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /residents?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
public class ResidentCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter firstname;

    private StringFilter lastname;

    private StringFilter email;

    private StringFilter phonenumber;

    private LongFilter roomId;

    private Boolean distinct;

    public ResidentCriteria() {}

    public ResidentCriteria(ResidentCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.firstname = other.firstname == null ? null : other.firstname.copy();
        this.lastname = other.lastname == null ? null : other.lastname.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.phonenumber = other.phonenumber == null ? null : other.phonenumber.copy();
        this.roomId = other.roomId == null ? null : other.roomId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ResidentCriteria copy() {
        return new ResidentCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getFirstname() {
        return firstname;
    }

    public StringFilter firstname() {
        if (firstname == null) {
            firstname = new StringFilter();
        }
        return firstname;
    }

    public void setFirstname(StringFilter firstname) {
        this.firstname = firstname;
    }

    public StringFilter getLastname() {
        return lastname;
    }

    public StringFilter lastname() {
        if (lastname == null) {
            lastname = new StringFilter();
        }
        return lastname;
    }

    public void setLastname(StringFilter lastname) {
        this.lastname = lastname;
    }

    public StringFilter getEmail() {
        return email;
    }

    public StringFilter email() {
        if (email == null) {
            email = new StringFilter();
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getPhonenumber() {
        return phonenumber;
    }

    public StringFilter phonenumber() {
        if (phonenumber == null) {
            phonenumber = new StringFilter();
        }
        return phonenumber;
    }

    public void setPhonenumber(StringFilter phonenumber) {
        this.phonenumber = phonenumber;
    }

    public LongFilter getRoomId() {
        return roomId;
    }

    public LongFilter roomId() {
        if (roomId == null) {
            roomId = new LongFilter();
        }
        return roomId;
    }

    public void setRoomId(LongFilter roomId) {
        this.roomId = roomId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResidentCriteria that = (ResidentCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(firstname, that.firstname) &&
            Objects.equals(lastname, that.lastname) &&
            Objects.equals(email, that.email) &&
            Objects.equals(phonenumber, that.phonenumber) &&
            Objects.equals(roomId, that.roomId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, email, phonenumber, roomId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResidentCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (firstname != null ? "firstname=" + firstname + ", " : "") +
            (lastname != null ? "lastname=" + lastname + ", " : "") +
            (email != null ? "email=" + email + ", " : "") +
            (phonenumber != null ? "phonenumber=" + phonenumber + ", " : "") +
            (roomId != null ? "roomId=" + roomId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
