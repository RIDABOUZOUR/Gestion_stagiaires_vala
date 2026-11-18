import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ConfirmOptions {
  title?: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
}

export interface ConfirmState extends ConfirmOptions {
  resolve: (value: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly stateSubject = new BehaviorSubject<ConfirmState | null>(null);
  readonly state$: Observable<ConfirmState | null> = this.stateSubject.asObservable();

  confirm(options: ConfirmOptions): Promise<boolean> {
    // Une confirmation encore ouverte est remplacée : on la considère comme annulée,
    // sinon sa promesse resterait en attente pour toujours.
    this.stateSubject.value?.resolve(false);
    return new Promise<boolean>((resolve) => {
      this.stateSubject.next({ ...options, resolve });
    });
  }

  respond(value: boolean): void {
    const state = this.stateSubject.value;
    if (state) {
      state.resolve(value);
      this.stateSubject.next(null);
    }
  }
}
