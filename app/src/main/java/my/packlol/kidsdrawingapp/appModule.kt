package my.packlol.kidsdrawingapp


import my.packlol.kidsdrawingapp.data.ImageSaver
import my.packlol.kidsdrawingapp.drawing.DrawingVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single {
        ImageSaver(androidContext())
    }

    viewModel {
        DrawingVM(get())
    }
}
