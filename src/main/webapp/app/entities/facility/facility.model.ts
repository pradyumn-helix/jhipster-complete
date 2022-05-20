import { IRoom } from 'app/entities/room/room.model';

export interface IFacility {
  id?: number;
  aC?: boolean | null;
  parking?: boolean | null;
  wifi?: boolean | null;
  room?: IRoom | null;
}

export class Facility implements IFacility {
  constructor(
    public id?: number,
    public aC?: boolean | null,
    public parking?: boolean | null,
    public wifi?: boolean | null,
    public room?: IRoom | null
  ) {
    this.aC = this.aC ?? false;
    this.parking = this.parking ?? false;
    this.wifi = this.wifi ?? false;
  }
}

export function getFacilityIdentifier(facility: IFacility): number | undefined {
  return facility.id;
}
