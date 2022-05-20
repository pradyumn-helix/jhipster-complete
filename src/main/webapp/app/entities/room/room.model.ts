import { IResident } from 'app/entities/resident/resident.model';

export interface IRoom {
  id?: number;
  roomno?: string;
  floor?: number | null;
  type?: string | null;
  residents?: IResident[] | null;
}

export class Room implements IRoom {
  constructor(
    public id?: number,
    public roomno?: string,
    public floor?: number | null,
    public type?: string | null,
    public residents?: IResident[] | null
  ) {}
}

export function getRoomIdentifier(room: IRoom): number | undefined {
  return room.id;
}
