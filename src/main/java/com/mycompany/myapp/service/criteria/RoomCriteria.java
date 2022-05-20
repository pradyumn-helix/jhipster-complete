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
 * Criteria class for the {@link com.mycompany.myapp.domain.Room} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.RoomResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /rooms?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
public class RoomCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter roomno;

    private IntegerFilter floor;

    private StringFilter type;

    private LongFilter residentId;

    private Boolean distinct;

    public RoomCriteria() {}

    public RoomCriteria(RoomCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.roomno = other.roomno == null ? null : other.roomno.copy();
        this.floor = other.floor == null ? null : other.floor.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.residentId = other.residentId == null ? null : other.residentId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public RoomCriteria copy() {
        return new RoomCriteria(this);
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

    public StringFilter getRoomno() {
        return roomno;
    }

    public StringFilter roomno() {
        if (roomno == null) {
            roomno = new StringFilter();
        }
        return roomno;
    }

    public void setRoomno(StringFilter roomno) {
        this.roomno = roomno;
    }

    public IntegerFilter getFloor() {
        return floor;
    }

    public IntegerFilter floor() {
        if (floor == null) {
            floor = new IntegerFilter();
        }
        return floor;
    }

    public void setFloor(IntegerFilter floor) {
        this.floor = floor;
    }

    public StringFilter getType() {
        return type;
    }

    public StringFilter type() {
        if (type == null) {
            type = new StringFilter();
        }
        return type;
    }

    public void setType(StringFilter type) {
        this.type = type;
    }

    public LongFilter getResidentId() {
        return residentId;
    }

    public LongFilter residentId() {
        if (residentId == null) {
            residentId = new LongFilter();
        }
        return residentId;
    }

    public void setResidentId(LongFilter residentId) {
        this.residentId = residentId;
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
        final RoomCriteria that = (RoomCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(roomno, that.roomno) &&
            Objects.equals(floor, that.floor) &&
            Objects.equals(type, that.type) &&
            Objects.equals(residentId, that.residentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomno, floor, type, residentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RoomCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (roomno != null ? "roomno=" + roomno + ", " : "") +
            (floor != null ? "floor=" + floor + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (residentId != null ? "residentId=" + residentId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
