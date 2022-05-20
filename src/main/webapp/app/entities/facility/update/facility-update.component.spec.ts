import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { FacilityService } from '../service/facility.service';
import { IFacility, Facility } from '../facility.model';
import { IRoom } from 'app/entities/room/room.model';
import { RoomService } from 'app/entities/room/service/room.service';

import { FacilityUpdateComponent } from './facility-update.component';

describe('Facility Management Update Component', () => {
  let comp: FacilityUpdateComponent;
  let fixture: ComponentFixture<FacilityUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let facilityService: FacilityService;
  let roomService: RoomService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [FacilityUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(FacilityUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(FacilityUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    facilityService = TestBed.inject(FacilityService);
    roomService = TestBed.inject(RoomService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call room query and add missing value', () => {
      const facility: IFacility = { id: 456 };
      const room: IRoom = { id: 42674 };
      facility.room = room;

      const roomCollection: IRoom[] = [{ id: 26756 }];
      jest.spyOn(roomService, 'query').mockReturnValue(of(new HttpResponse({ body: roomCollection })));
      const expectedCollection: IRoom[] = [room, ...roomCollection];
      jest.spyOn(roomService, 'addRoomToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ facility });
      comp.ngOnInit();

      expect(roomService.query).toHaveBeenCalled();
      expect(roomService.addRoomToCollectionIfMissing).toHaveBeenCalledWith(roomCollection, room);
      expect(comp.roomsCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const facility: IFacility = { id: 456 };
      const room: IRoom = { id: 66766 };
      facility.room = room;

      activatedRoute.data = of({ facility });
      comp.ngOnInit();

      expect(comp.editForm.value).toEqual(expect.objectContaining(facility));
      expect(comp.roomsCollection).toContain(room);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Facility>>();
      const facility = { id: 123 };
      jest.spyOn(facilityService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ facility });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: facility }));
      saveSubject.complete();

      // THEN
      expect(comp.previousState).toHaveBeenCalled();
      expect(facilityService.update).toHaveBeenCalledWith(facility);
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Facility>>();
      const facility = new Facility();
      jest.spyOn(facilityService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ facility });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: facility }));
      saveSubject.complete();

      // THEN
      expect(facilityService.create).toHaveBeenCalledWith(facility);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Facility>>();
      const facility = { id: 123 };
      jest.spyOn(facilityService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ facility });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(facilityService.update).toHaveBeenCalledWith(facility);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Tracking relationships identifiers', () => {
    describe('trackRoomById', () => {
      it('Should return tracked Room primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackRoomById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });
  });
});
