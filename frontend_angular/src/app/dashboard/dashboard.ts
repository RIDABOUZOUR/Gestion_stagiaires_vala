import { Component, OnInit } from '@angular/core';
import Chart from 'chart.js/auto';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: false
})
export class Dashboard implements OnInit {
  public chartDepartement: any;
  public chartFiliere: any;
  public chartOrganisme: any;
  public chartEncadrant: any; 

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchDepartementData();
    this.fetchFiliereData();
    this.fetchOrganismeData();
    this.fetchEncadrantData(); 
  }

  fetchDepartementData() {
    this.http.get<{ [key: string]: number }>(environment.apiUrl + '/statistiques/departements')
      .subscribe(data => {
        const labels = Object.keys(data);
        const values = Object.values(data);

        if (this.chartDepartement) this.chartDepartement.destroy();

        this.chartDepartement = new Chart("stagiairesChart", {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: "Stagiaires / Département",
              data: values,
              backgroundColor: 'rgba(54, 162, 235, 0.6)'
            }]
          },
          options: {
            responsive: true,
            plugins: {
              title: {
                display: true,
                text: 'Nombre de stagiaires par département'
              }
            }
          }
        });
      });
  }

  fetchFiliereData() {
    this.http.get<{ [key: string]: number }>(environment.apiUrl + '/statistiques/filieres')
      .subscribe(data => {
        const labels = Object.keys(data);
        const values = Object.values(data);

        if (this.chartFiliere) this.chartFiliere.destroy();

        this.chartFiliere = new Chart("filiereChart", {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: "Stagiaires / Filière",
              data: values,
              backgroundColor: 'rgba(75, 192, 192, 0.6)'
            }]
          },
          options: {
            responsive: true,
            plugins: {
              title: {
                display: true,
                text: 'Nombre de stagiaires par filière'
              }
            }
          }
        });
      });
  }

  fetchOrganismeData() {
    this.http.get<{ [key: string]: number }>(environment.apiUrl + '/statistiques/organismes')
      .subscribe(data => {
        const labels = Object.keys(data);
        const values = Object.values(data);

        if (this.chartOrganisme) this.chartOrganisme.destroy();

        this.chartOrganisme = new Chart("organismeChart", {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: "Stagiaires / Organisme",
              data: values,
              backgroundColor: 'rgba(255, 159, 64, 0.6)'
            }]
          },
          options: {
            responsive: true,
            plugins: {
              title: {
                display: true,
                text: 'Nombre de stagiaires par organisme'
              }
            }
          }
        });
      });
  }

  fetchEncadrantData() {
    this.http.get<{ [key: string]: number }>(environment.apiUrl + '/statistiques/encadrants')
      .subscribe(data => {
        const labels = Object.keys(data);
        const values = Object.values(data);

        if (this.chartEncadrant) this.chartEncadrant.destroy();

        this.chartEncadrant = new Chart("encadrantChart", {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: "Stagiaires / Encadrant",
              data: values,
              backgroundColor: 'rgba(153, 102, 255, 0.6)'
            }]
          },
          options: {
            responsive: true,
            plugins: {
              title: {
                display: true,
                text: 'Nombre de stagiaires par encadrant'
              }
            }
          }
        });
      });
  }
}
