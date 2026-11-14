import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Departement } from '../services/departement';
import { DepartementModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

@Component({
  selector: 'app-new-deapartement',
  standalone: false,
  templateUrl: './new-deapartement.html',
  styleUrl: './new-deapartement.css',
})
export class NewDeapartement implements OnInit {
  form!: FormGroup;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly departementService: Departement,
    private readonly router: Router,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      responsable: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  handleSaveDepartement(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Veuillez corriger les champs en surbrillance.');
      return;
    }

    const value = this.form.value;
    const payload: DepartementModel = {
      id: 0,
      nom: value.nom,
      responsable: value.responsable,
    };

    this.saving = true;
    this.departementService
      .addDepartement(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le département a été enregistré avec succès.');
          this.router.navigate(['/departements']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
