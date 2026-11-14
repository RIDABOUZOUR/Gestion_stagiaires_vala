import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditStagiaire } from './edit-stagiaire';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('EditStagiaire', () => {
  let component: EditStagiaire;
  let fixture: ComponentFixture<EditStagiaire>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [EditStagiaire],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(EditStagiaire);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
