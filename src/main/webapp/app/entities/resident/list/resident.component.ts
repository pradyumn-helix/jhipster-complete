import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IResident } from '../resident.model';
import { ResidentService } from '../service/resident.service';
import { ResidentDeleteDialogComponent } from '../delete/resident-delete-dialog.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'jhi-resident',
  templateUrl: './resident.component.html',
})
export class ResidentComponent implements OnInit {
  residents?: IResident[];
  isLoading = false;
  rooms = this.route.snapshot.params['id'];

  constructor(protected residentService: ResidentService, protected modalService: NgbModal, private route: ActivatedRoute) {}

  loadAllA(num: number): void {
    this.isLoading = true;

    this.residentService.query({ 'roomId.equals': num }).subscribe({
      next: (res: HttpResponse<IResident[]>) => {
        this.isLoading = false;
        this.residents = res.body ?? [];
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  loadAll(): void {
    this.isLoading = true;

    this.residentService.query().subscribe({
      next: (res: HttpResponse<IResident[]>) => {
        this.isLoading = false;
        this.residents = res.body ?? [];
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }
  ngOnInit(): void {
    if (this.rooms) {
      this.loadAllA(this.rooms);
      console.warn('PARAMS', this.rooms);
    } else {
      this.loadAll();
      console.warn('WITHOUT PARAMS', this.rooms);
    }
  }

  trackId(_index: number, item: IResident): number {
    return item.id!;
  }

  delete(resident: IResident): void {
    const modalRef = this.modalService.open(ResidentDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.resident = resident;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.loadAll();
      }
    });
  }
}
