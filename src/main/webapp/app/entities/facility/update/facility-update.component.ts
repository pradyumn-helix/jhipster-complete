import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IFacility, Facility } from '../facility.model';
import { FacilityService } from '../service/facility.service';
import { IRoom } from 'app/entities/room/room.model';
import { RoomService } from 'app/entities/room/service/room.service';

@Component({
  selector: 'jhi-facility-update',
  templateUrl: './facility-update.component.html',
})
export class FacilityUpdateComponent implements OnInit {
  isSaving = false;

  roomsCollection: IRoom[] = [];

  editForm = this.fb.group({
    id: [],
    aC: [],
    parking: [],
    wifi: [],
    room: [],
  });

  constructor(
    protected facilityService: FacilityService,
    protected roomService: RoomService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ facility }) => {
      this.updateForm(facility);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const facility = this.createFromForm();
    if (facility.id !== undefined) {
      this.subscribeToSaveResponse(this.facilityService.update(facility));
    } else {
      this.subscribeToSaveResponse(this.facilityService.create(facility));
    }
  }

  trackRoomById(_index: number, item: IRoom): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IFacility>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(facility: IFacility): void {
    this.editForm.patchValue({
      id: facility.id,
      aC: facility.aC,
      parking: facility.parking,
      wifi: facility.wifi,
      room: facility.room,
    });

    this.roomsCollection = this.roomService.addRoomToCollectionIfMissing(this.roomsCollection, facility.room);
  }

  protected loadRelationshipsOptions(): void {
    this.roomService
      .query({ 'facilityId.specified': 'false' })
      .pipe(map((res: HttpResponse<IRoom[]>) => res.body ?? []))
      .pipe(map((rooms: IRoom[]) => this.roomService.addRoomToCollectionIfMissing(rooms, this.editForm.get('room')!.value)))
      .subscribe((rooms: IRoom[]) => (this.roomsCollection = rooms));
  }

  protected createFromForm(): IFacility {
    return {
      ...new Facility(),
      id: this.editForm.get(['id'])!.value,
      aC: this.editForm.get(['aC'])!.value,
      parking: this.editForm.get(['parking'])!.value,
      wifi: this.editForm.get(['wifi'])!.value,
      room: this.editForm.get(['room'])!.value,
    };
  }
}
