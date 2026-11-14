import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Departement } from '../services/departement';

@Component({
  selector: 'app-edit-departement',
  templateUrl: './edit-departement.html',
  standalone: false
})
export class EditDepartement implements OnInit {
  departementForm!: FormGroup;
  departementId!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private departementService: Departement
  ) {}

  ngOnInit(): void {
    this.departementId = +this.route.snapshot.paramMap.get('id')!;
    this.departementService.getDepartementById(this.departementId).subscribe(data => {
      this.departementForm = this.fb.group({
        nom: [data.nom],
        responsable: [data.responsable]
      });
    });
  }

 onUpdate() {
  if (this.departementForm.valid) {
    this.departementService.editDepartement(this.departementId, this.departementForm.value)
      .subscribe({
        next: () => {
          alert("Le département a été mis à jour avec succès");
          this.router.navigate(['/departements']);
        },
        error: (err) => {
          console.error(err);
          alert("Une erreur est survenue lors de la mise à jour");
        }
      });
  }
}

}
