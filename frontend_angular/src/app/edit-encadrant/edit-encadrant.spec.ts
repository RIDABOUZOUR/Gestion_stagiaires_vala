import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditEncadrant } from './edit-encadrant';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('EditEncadrant', () => {
  let component: EditEncadrant;
  let fixture: ComponentFixture<EditEncadrant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [EditEncadrant],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(EditEncadrant);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
