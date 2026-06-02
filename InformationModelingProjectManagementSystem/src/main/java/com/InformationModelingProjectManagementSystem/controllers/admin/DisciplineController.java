package com.InformationModelingProjectManagementSystem.controllers.admin;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.InformationModelingProjectManagementSystem.models.Discipline;
import com.InformationModelingProjectManagementSystem.services.DisciplineService;
import com.InformationModelingProjectManagementSystem.util.DisciplineValidator;

@Controller
@RequestMapping("/adminpage/disciplines")
public class DisciplineController {

    private final DisciplineService disciplineService;
    private final DisciplineValidator disciplineValidator;

    @Autowired
    public DisciplineController(DisciplineService disciplineService, DisciplineValidator disciplineValidator) {
        this.disciplineService = disciplineService;
        this.disciplineValidator = disciplineValidator;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("disciplines", disciplineService.findAll());
        return "admin/disciplines/list";
    }

    @GetMapping("/sort")
    public String sortPage(Model model) {
        model.addAttribute("disciplines", disciplineService.findAll());
        return "admin/disciplines/sort";
    }

    @PostMapping("/update-order")
    public String updateOrder(@RequestParam("orderIds") String orderIds, RedirectAttributes redirectAttributes) {
        String[] idsArray = orderIds.split(",");
        for (int i = 0; i < idsArray.length; i++) {
            int id = Integer.parseInt(idsArray[i]);
            Discipline d = disciplineService.findById(id).orElse(null);
            if (d != null) {
                d.setSortOrder(i);
                disciplineService.save(d);
            }
        }
        redirectAttributes.addFlashAttribute("success", "Порядок разделов обновлён");
        return "redirect:/adminpage/disciplines";
    }

    @GetMapping("/create")
    public String createForm(@ModelAttribute("discipline") Discipline discipline) {
        return "admin/disciplines/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("discipline") Discipline discipline,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        disciplineValidator.validate(discipline, bindingResult);
        if (bindingResult.hasErrors()) return "admin/disciplines/form";
        
        discipline.setSortOrder(disciplineService.getNextSortOrder());
        disciplineService.save(discipline);

        redirectAttributes.addFlashAttribute("success", "Раздел \"" + discipline.getShortName() + "\" создан");
        return "redirect:/adminpage/disciplines";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return disciplineService.findById(id).map(discipline -> {
            model.addAttribute("discipline", discipline);
            return "admin/disciplines/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Раздел не найден");
            return "redirect:/adminpage/disciplines";
        });
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute("discipline") Discipline discipline,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        discipline.setId(id);
        disciplineValidator.validate(discipline, bindingResult);
        if (bindingResult.hasErrors()) return "admin/disciplines/form";
        disciplineService.save(discipline);
        redirectAttributes.addFlashAttribute("success", "Раздел \"" + discipline.getShortName() + "\" обновлён");
        return "redirect:/adminpage/disciplines";
    }

    @PostMapping("/{id}/toggle")
    public String toggleVisible(@PathVariable int id, RedirectAttributes redirectAttributes) {
        disciplineService.findById(id).ifPresent(discipline -> {
            discipline.setVisible(!discipline.isVisible());
            disciplineService.save(discipline);
            redirectAttributes.addFlashAttribute("success", "Видимость изменена");
        });
        return "redirect:/adminpage/disciplines";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (disciplineService.isUsedInTasks(id)) {
            redirectAttributes.addFlashAttribute("error", "Раздел используется в задачах. Сначала замените его на другой.");
            return "redirect:/adminpage/disciplines";
        }
        if (disciplineService.deleteById(id)) {
            redirectAttributes.addFlashAttribute("success", "Раздел удалён");
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить раздел (возможно, он используется)");
        }
        return "redirect:/adminpage/disciplines";
    }

    // Замена раздела во всех задачах (при использовании)
    @PostMapping("/replace")
    public String replaceDiscipline(@RequestParam int oldId, @RequestParam int newId, RedirectAttributes redirectAttributes) {
        if (oldId == newId) {
            redirectAttributes.addFlashAttribute("error", "Разделы должны быть разными");
            return "redirect:/adminpage/disciplines";
        }
        disciplineService.replaceDisciplineInTasks(oldId, newId);
        disciplineService.deleteById(oldId);
        redirectAttributes.addFlashAttribute("success", "Раздел заменён и удалён");
        return "redirect:/adminpage/disciplines";
    }
    
}