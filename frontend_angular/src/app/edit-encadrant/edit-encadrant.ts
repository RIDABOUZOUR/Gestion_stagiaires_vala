import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Encadrant } from '../services/encadrant';

@Component({
  selector: 'app-edit-encadrant',
  templateUrl: './edit-encadrant.html',
  standalone: false
})
export class EditEncadrant implements OnInit {
  encadrantForm!: FormGroup;
  encadrantId!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private encadrantService: Encadrant
  ) {}

  ngOnInit(): void {
    this.encadrantId = +this.route.snapshot.paramMap.get('id')!;
    this.encadrantService.getEncadrantById(this.encadrantId).subscribe(data => {
      this.encadrantForm = this.fb.group({
        nom: [data.nom],
        prenom: [data.prenom],
        email: [data.email],
        telephone: [data.telephone]

      });
    });
  }

  onUpdate() {
  if (this.encadrantForm.valid) {
    this.encadrantService.editEncadrant(this.encadrantId, this.encadrantForm.value)
      .subscribe({
        next: () => {
          alert("L'encadrant a été mis à jour avec succès");
          this.router.navigate(['/encadrants']);
        },
        error: (err) => {
          console.error(err);
          alert("Une erreur est survenue lors de la mise à jour");
        }
      });
  }
}

}