import { IRoom } from 'app/entities/room/room.model';

export interface IResident {
  id?: number;
  firstname?: string | null;
  lastname?: string | null;
  email?: string;
  phonenumber?: string | null;
  room?: IRoom | null;
}

export class Resident implements IResident {
  constructor(
    public id?: number,
    public firstname?: string | null,
    public lastname?: string | null,
    public email?: string,
    public phonenumber?: string | null,
    public room?: IRoom | null
  ) {}
}

export function getResidentIdentifier(resident: IResident): number | undefined {
  return resident.id;
}
