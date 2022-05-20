import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { IRoom } from 'app/entities/room/room.model';
import { RoomService } from 'app/entities/room/service/room.service';
import { HttpResponse } from '@angular/common/http';
import { FacilityService } from 'app/entities/facility/service/facility.service';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  rooms?: IRoom[];
  filterdata?: any;
  private readonly destroy$ = new Subject<void>();

  constructor(
    protected roomService: RoomService,
    private accountService: AccountService,
    private router: Router,
    private facilityService: FacilityService
  ) {}

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
    this.getRooms();
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getFacilities(id: number | undefined): any {
    this.router.navigate([`/facility/${id!}/view`]);
  }

  getResidents(roomid: number | undefined): any {
    this.router.navigate([`/resident/${roomid!}`]);
  }

  getRoomsFiltered(type: string | undefined): any {
    switch (type) {
      case 'aC':
        this.facilityService.query({ 'aC.in': [true], 'roomidId.greaterThan': 0, 'roomId.specified': true }).subscribe(res => {
          this.filterdata = res.body;
          console.warn('RESPOSE BEFORE', this.filterdata);
          this.rooms = [];
          this.filterdata.forEach((element: any) => {
            this.rooms?.push(element['room']);
          });
          console.warn('RESPOSE AFTER', this.rooms);
        });
        break;
      case 'wifi':
        this.facilityService.query({ 'wifi.in': [true], 'roomidId.greaterThan': 0, 'roomId.specified': true }).subscribe(res => {
          this.filterdata = res.body;
          this.rooms = [];
          this.filterdata.forEach((element: any) => {
            this.rooms?.push(element['room']);
          });
        });
        break;
      case 'parking':
        this.facilityService.query({ 'parking.in': [true], 'roomidId.greaterThan': 0, 'roomId.specified': true }).subscribe(res => {
          this.filterdata = res.body;
          this.rooms = [];
          this.filterdata.forEach((element: any) => {
            this.rooms?.push(element['room']);
            console.warn('After Push');
          });
        });
        break;
      default:
        this.getRooms();
        break;
    }
  }

  getRooms(): any {
    this.roomService.query().subscribe({
      next: (res: HttpResponse<IRoom[]>) => {
        this.rooms = res.body ?? [];
        console.warn(this.rooms);
      },
      error() {
        console.warn('Error');
      },
    });
  }
}
