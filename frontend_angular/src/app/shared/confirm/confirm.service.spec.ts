import { ConfirmService } from './confirm.service';

describe('ConfirmService', () => {
  let service: ConfirmService;

  beforeEach(() => (service = new ConfirmService()));

  it('résout la promesse avec la réponse et ferme le dialogue', async () => {
    const reponse = service.confirm({ message: 'Supprimer ?' });
    service.respond(true);

    expect(await reponse).toBeTrue();
    let etat: unknown = 'non lu';
    service.state$.subscribe((s) => (etat = s));
    expect(etat).toBeNull();
  });

  it('une nouvelle confirmation annule la précédente au lieu de la laisser en attente', async () => {
    const premiere = service.confirm({ message: 'Premier' });
    const seconde = service.confirm({ message: 'Second' });
    service.respond(true);

    expect(await premiere).toBeFalse();
    expect(await seconde).toBeTrue();
  });

  it('respond sans dialogue ouvert ne fait rien', () => {
    expect(() => service.respond(false)).not.toThrow();
  });
});
